package com.juliet.flow.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.juliet.flow.client.common.thread.ThreadPoolFactory;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.dao.*;
import com.juliet.flow.domain.entity.*;
import com.juliet.flow.domain.model.*;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.repository.trasnfer.FlowEntityFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Repository
@Slf4j
public class FlowRepositoryImpl implements FlowRepository {

    @Autowired
    private FlowDao flowDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private FormDao formDao;

    @Autowired
    private FieldDao fieldDao;

    @Autowired
    private PostDao postDao;

    @Autowired
    private SupplierDao supplierDao;

    @Autowired
    private FlowTemplateDao flowTemplateDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long add(Flow flow) {
        FlowEntityFactory.cleanFlowId(flow);
        FlowEntity entity = FlowEntityFactory.toFlowEntity(flow);
        flowDao.insert(entity);
        flow.setId(entity.getId());
        addNodes(flow.getNodes(), entity.getId(), 0L);
        return entity.getId();
    }

    private void addNodes(List<Node> nodes, Long flowId, Long flowTemplateId) {
        List<NodeEntity> nodeEntities = FlowEntityFactory.transferNodeEntities(nodes, flowId, flowTemplateId);
        nodeDao.insertBatch(nodeEntities);

        List<FormEntity> formEntities = FlowEntityFactory.transferFormEntities(nodes);
        formDao.insertBatch(formEntities);

        List<FieldEntity> fieldEntities = FlowEntityFactory.transferFieldEntities(nodes);
        List<List<FieldEntity>> parts = Lists.partition(fieldEntities, 50);
        for (List<FieldEntity> part : parts) {
            fieldDao.insertBatch(part);
        }

        List<PostEntity> postEntities = FlowEntityFactory.transferPostEntity(nodes);
        if (CollectionUtils.isNotEmpty(postEntities)) {
            postDao.insertBatch(postEntities);
        }

        List<SupplierEntity> supplierEntities = FlowEntityFactory.transferSupplierEntity(nodes);
        if (CollectionUtils.isNotEmpty(supplierEntities)) {
            supplierDao.insertBatch(supplierEntities);
        }
    }

    @Override
    public void addTemplate(FlowTemplate flowTemplate) {
        FlowTemplateEntity entity = FlowEntityFactory.toFlowTemplateEntity(flowTemplate);
        flowTemplateDao.insert(entity);
        fillUserId(flowTemplate.getNodes(), flowTemplate.getCreateBy(), flowTemplate.getUpdateBy());
        addNodes(flowTemplate.getNodes(), 0L, entity.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(Flow flow) {
        FlowEntity flowEntity = FlowEntityFactory.toFlowEntity(flow);
        flowDao.updateById(flowEntity);
        deleteNodes(flow.getNodes());
        addNodes(flow.getNodes(), flow.getId(), 0L);
    }

    @Override
    public void updateTemplate(FlowTemplate flowTemplate) {
        FlowTemplate flowTemplateOld = queryTemplateById(flowTemplate.getId());
        BusinessAssert.assertNotNull(flowTemplateOld, StatusCode.ILLEGAL_PARAMS,
            "找不到模板，id：" + flowTemplate.getId());
        flowTemplate.setCreateBy(flowTemplateOld.getCreateBy());
        FlowTemplateEntity flowTemplateEntity = FlowEntityFactory.toFlowTemplateEntity(flowTemplate);
        flowTemplateDao.updateById(flowTemplateEntity);
        deleteNodes(flowTemplateOld.getNodes());
        fillUserId(flowTemplate.getNodes(), flowTemplate.getCreateBy(), flowTemplate.getUpdateBy());
        addNodes(flowTemplate.getNodes(), 0L, flowTemplate.getId());
    }

    private void fillUserId(List<Node> nodes, Long createBy, Long updateBy) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            node.setCreateBy(createBy);
            node.setUpdateBy(updateBy);
            if (node.getForm() != null) {
                node.getForm().setCreateBy(createBy);
                node.getForm().setUpdateBy(updateBy);
                if (!CollectionUtils.isEmpty(node.getForm().getFields())) {
                    for (Field field : node.getForm().getFields()) {
                        field.setCreateBy(createBy);
                        field.setUpdateBy(updateBy);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(node.getBindPosts())) {
                for (Post post : node.getBindPosts()) {
                    post.setCreateBy(createBy);
                    post.setUpdateBy(updateBy);
                }
            }
            if (!CollectionUtils.isEmpty(node.getBindSuppliers())) {
                for (Supplier supplier : node.getBindSuppliers()) {
                    supplier.setCreateBy(createBy);
                    supplier.setUpdateBy(updateBy);
                }
            }
        }
    }

    @Override
    public Flow queryById(Long id) {
        FlowEntity flowEntity = flowDao.selectById(id);
        if (flowEntity == null) {
            return null;
        }
        FlowTemplateEntity template = flowTemplateDao.selectById(flowEntity.getFlowTemplateId());
        Flow flow = FlowEntityFactory.toFlow(flowEntity);
        flow.setTemplateCode(template.getCode());
        List<Node> nodes = getNodes(flowEntity.getId());
        flow.setNodes(nodes);
        return flow;
    }

    @Override
    public Flow queryLatestByParentId(Long id) {
        List<Flow> flows = listFlowByParentId(id);
        if (CollectionUtils.isEmpty(flows)) {
            return null;
        }
        Flow latestFlow = null;
        for (Flow flow : flows) {
            if (latestFlow == null) {
                latestFlow = flow;
                continue;
            }
            // 获取创建时间最晚的
            if (flow.getCreateTime().after(latestFlow.getCreateTime())) {
                latestFlow = flow;
            }
        }
        return latestFlow;
    }

    @Override
    public List<Flow> queryByIdList(List<Long> idList) {
        List<FlowEntity> flowList = flowDao.selectList(
            Wrappers.<FlowEntity>lambdaQuery().in(FlowEntity::getId, idList));
        return assembleFlow(flowList);
    }

    @Override
    public List<Flow> queryOnlyFlowByIdList(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.emptyList();
        }
        if (idList.size() <= 50) {
            List<FlowEntity> flowList = flowDao.selectList(
                Wrappers.<FlowEntity>lambdaQuery().in(FlowEntity::getId, idList));
            return flowList.stream()
                .map(FlowEntityFactory::toFlow)
                .collect(Collectors.toList());
        }

        List<Future<List<FlowEntity>>> futureList = new ArrayList<>();
        List<List<Long>> parts = Lists.partition(idList, 40);
        parts.forEach(part -> {
            Future<List<FlowEntity>> future = ThreadPoolFactory.THREAD_POOL_TODO_MAIN.submit(() ->
                flowDao.selectList(Wrappers.<FlowEntity>lambdaQuery().in(FlowEntity::getId, idList)));
            futureList.add(future);
        });

        List<Flow> flowList = futureList.stream()
            .map(ThreadPoolFactory::get)
            .flatMap(Collection::stream)
            .map(FlowEntityFactory::toFlow)
            .collect(Collectors.toList());

        List<Long> templateIdList = flowList.stream()
            .map(Flow::getFlowTemplateId)
            .filter(Objects::nonNull).distinct()
            .collect(Collectors.toList());

        Map<Long, String> codeMap = flowTemplateDao.selectBatchIds(templateIdList).stream()
            .collect(Collectors.toMap(FlowTemplateEntity::getId, FlowTemplateEntity::getCode, (v1, v2) -> v1));
        return flowList.stream()
            .peek(flow -> flow.setTemplateCode(codeMap.get(flow.getFlowTemplateId())))
            .collect(Collectors.toList());
    }

    @Override
    public List<Flow> listFlowByIdOrParentId(List<Long> idList) {
        List<FlowEntity> flowList = flowDao.selectList(
            Wrappers.<FlowEntity>lambdaQuery().in(FlowEntity::getId, idList)
                .or()
                .in(FlowEntity::getParentId, idList));
        return assembleFlow(flowList);
    }

    @Override
    public List<Flow> listFlowByParentId(Long id) {
        List<FlowEntity> flowEntities = flowDao.selectList(Wrappers.<FlowEntity>lambdaQuery()
            .eq(FlowEntity::getParentId, id));
        if (CollectionUtils.isEmpty(flowEntities)) {
            return Lists.newArrayList();
        }
        return assembleFlow(flowEntities);
    }

    @Override
    public List<Flow> listFlowByParentId(Collection<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.emptyList();
        }
        List<FlowEntity> flowEntities = flowDao.selectList(Wrappers.<FlowEntity>lambdaQuery()
            .in(FlowEntity::getParentId, idList));
        if (CollectionUtils.isEmpty(flowEntities)) {
            return Lists.newArrayList();
        }
        return assembleFlow(flowEntities);
    }

    @Override
    public List<Flow> queryMainFlowById(Collection<Long> idList) {
        List<FlowEntity> flowEntities = flowDao.selectList(Wrappers.<FlowEntity>lambdaQuery()
            .in(FlowEntity::getId, idList)
            .eq(FlowEntity::getParentId, 0)
        );
        return assembleFlow(flowEntities);
    }

    @Override
    public void updateStatusById(FlowStatusEnum status, Long id) {
        FlowEntity flowEntity = new FlowEntity();
        flowEntity.setId(id);
        flowEntity.setStatus(status.getCode());
        flowDao.updateById(flowEntity);
    }

    @Override
    public FlowTemplate queryTemplateById(Long id) {
        FlowTemplateEntity flowTemplateEntity = flowTemplateDao.selectById(id);
        if (flowTemplateEntity == null) {
            return null;
        }
        FlowTemplate flowTemplate = FlowEntityFactory.toFlowTemplate(flowTemplateEntity);
        flowTemplate.setNodes(getTemplateStartNodes(flowTemplateEntity.getId()));
        return flowTemplate;
    }

    @Override
    public FlowTemplate queryTemplateByCode(String code, Long tenantId) {
        FlowTemplateEntity flowTemplateEntity = flowTemplateDao.selectOne(
            Wrappers.<FlowTemplateEntity>lambdaQuery().eq(FlowTemplateEntity::getCode, code)
                .eq(FlowTemplateEntity::getTenantId, tenantId)
                .last("limit 1"));
        if (flowTemplateEntity == null) {
            return null;
        }
        FlowTemplate flowTemplate = FlowEntityFactory.toFlowTemplate(flowTemplateEntity);
        flowTemplate.setNodes(getTemplateStartNodes(flowTemplateEntity.getId()));
        return flowTemplate;
    }

    @Override
    public void updateFlowTemplateStatusById(FlowTemplateStatusEnum status, Long id) {
        FlowTemplateEntity flowTemplateEntity = new FlowTemplateEntity();
        flowTemplateEntity.setId(id);
        flowTemplateEntity.setStatus(status.getCode());
        flowTemplateDao.updateById(flowTemplateEntity);
    }

    @Override
    public List<Node> listNode(NodeQuery query) {
        List<NodeEntity> nodeEntities = nodeDao.listNode(query);
        return assembleNode(nodeEntities);
    }

    @Override
    public List<Node> listNode(Long supplierId, String supplierType) {
        if (supplierId == null || StringUtils.isBlank(supplierType)) {
            return Collections.emptyList();
        }
        List<Long> nodeIdList = supplierDao.selectList(
                Wrappers.<SupplierEntity>lambdaQuery().eq(SupplierEntity::getSupplierId, supplierId)
                    .eq(SupplierEntity::getSupplierType, supplierType)
            )
            .stream()
            .map(SupplierEntity::getNodeId)
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(nodeIdList)) {
            return Collections.emptyList();
        }

        return assembleNode(nodeDao.selectBatchIds(nodeIdList));
    }

    @Override
    public Node queryNodeById(Long nodeId) {
        return FlowEntityFactory.toSingleNode(nodeDao.selectById(nodeId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFlow(Long id) {
        flowDao.deleteById(id);
        flowDao.delete(Wrappers.<FlowEntity>lambdaQuery().eq(FlowEntity::getParentId, id));
        List<Long> idList = flowDao.selectList(
                Wrappers.<FlowEntity>lambdaQuery().select(FlowEntity::getId).eq(FlowEntity::getParentId, id))
            .stream()
            .map(FlowEntity::getId)
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(idList)) {
            nodeDao.delete(Wrappers.<NodeEntity>lambdaQuery().in(NodeEntity::getFlowId, idList));
        }
    }

    private void deleteNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        List<Long> nodeIds = nodes.stream().map(Node::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(nodeIds)) {
            nodeDao.delete(Wrappers.<NodeEntity>lambdaUpdate()
                .in(NodeEntity::getId, nodeIds));
            postDao.delete(Wrappers.<PostEntity>lambdaUpdate()
                .in(PostEntity::getNodeId, nodeIds));
            formDao.delete(Wrappers.<FormEntity>lambdaUpdate()
                .in(FormEntity::getNodeId, nodeIds));
        }
        List<Long> formIds = nodes.stream()
            .filter(node -> node.getForm() != null)
            .map(node -> node.getForm().getId())
            .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(formIds)) {
            fieldDao.delete(Wrappers.<FieldEntity>lambdaQuery()
                .in(FieldEntity::getFormId, formIds));
        }
    }

    /**
     * 获取流程模板的开始节点
     */
    private List<Node> getTemplateStartNodes(Long templateFlowId) {
        List<NodeEntity> nodeEntities = nodeDao.selectList(Wrappers.<NodeEntity>lambdaUpdate()
            .eq(NodeEntity::getFlowTemplateId, templateFlowId));
        return assembleNode(nodeEntities);
    }

    /**
     * 获取某个流程的开始节点
     */
    private List<Node> getNodes(Long flowId) {
        List<NodeEntity> nodeEntities = nodeDao.selectList(Wrappers.<NodeEntity>lambdaQuery()
            .eq(NodeEntity::getFlowId, flowId));
        return assembleNode(nodeEntities);
    }

    /**
     * 组装Node
     */
    private List<Node> assembleNode(List<NodeEntity> nodeEntities) {
        List<Node> nodes = FlowEntityFactory.toNodes(nodeEntities);
        if (CollectionUtils.isEmpty(nodes)) {
            return Lists.newArrayList();
        }
        // 填充表单信息
        List<Long> nodeIds = nodeEntities.stream().map(NodeEntity::getId).collect(Collectors.toList());
        List<FormEntity> formEntities = formDao.selectList(Wrappers.<FormEntity>lambdaQuery()
            .in(FormEntity::getNodeId, nodeIds));
        FlowEntityFactory.fillNodeForm(nodes, formEntities);
        // 填充字段信息
        List<FieldEntity> fieldEntities = fieldDao.selectList(Wrappers.<FieldEntity>lambdaQuery()
            .in(CollectionUtils.isNotEmpty(
                    formEntities.stream().map(FormEntity::getId).distinct().collect(Collectors.toList())),
                FieldEntity::getFormId,
                formEntities.stream().map(FormEntity::getId).distinct().collect(Collectors.toList())));
        FlowEntityFactory.fillNodeField(nodes, fieldEntities);
        // 填充岗位信息
        List<PostEntity> postEntities = postDao.selectList(Wrappers.<PostEntity>lambdaQuery()
            .in(PostEntity::getNodeId, nodeIds));
        FlowEntityFactory.fillNodePost(nodes, postEntities);
        return nodes;
    }

    public List<Flow> assembleFlow(List<FlowEntity> flowList) {
        if (CollectionUtils.isEmpty(flowList)) {
            return Collections.emptyList();
        }
        List<Long> flowIdList = flowList.stream().map(FlowEntity::getId).collect(Collectors.toList());
        List<NodeEntity> nodeEntityList = nodeDao.selectList(Wrappers.<NodeEntity>lambdaQuery()
            .in(NodeEntity::getFlowId, flowIdList)
        );
        Map<Long, List<NodeEntity>> nodeMap = nodeEntityList.stream()
            .collect(Collectors.groupingBy(NodeEntity::getFlowId));
        List<Long> nodeIdList = nodeEntityList.stream().map(NodeEntity::getId).collect(Collectors.toList());

        Future<List<PostEntity>> futurePostEntities = ThreadPoolFactory.THREAD_POOL_TODO_MAIN.submit(
            () -> postDao.selectList(Wrappers.<PostEntity>lambdaQuery()
                .in(PostEntity::getNodeId, nodeIdList)));

        Future<List<SupplierEntity>> futureSupplierEntities = ThreadPoolFactory.THREAD_POOL_TODO_MAIN.submit(
            () -> supplierDao.selectList(Wrappers.<SupplierEntity>lambdaQuery()
                .in(SupplierEntity::getNodeId, nodeIdList)));

        List<FormEntity> formEntities = formDao.selectList(Wrappers.<FormEntity>lambdaQuery()
            .in(FormEntity::getNodeId, nodeIdList));
        List<Long> formIdList = formEntities.stream().map(FormEntity::getId).distinct().collect(Collectors.toList());

//        List<FieldEntity> fieldEntities = fieldDao.selectList(Wrappers.<FieldEntity>lambdaQuery()
//            .in(FieldEntity::getFormId, formIdList));
        List<FieldEntity> fieldEntities = parallelBatchQueryFieldEntities(formIdList);
        List<PostEntity> postEntities = ThreadPoolFactory.get(futurePostEntities);
        List<SupplierEntity> supplierEntities = ThreadPoolFactory.get(futureSupplierEntities);

        List<Long> flowTemplateIds = flowList.stream().map(FlowEntity::getFlowTemplateId).collect(Collectors.toList());
        List<FlowTemplateEntity> flowTemplateEntities = flowTemplateDao.selectBatchIds(flowTemplateIds);
        Map<Long, String> flowTemplateCodeMap = flowTemplateEntities.stream()
            .collect(Collectors.toMap(FlowTemplateEntity::getId, FlowTemplateEntity::getCode));

        return flowList.stream()
            .map(flowEntity -> {
                Flow flow = FlowEntityFactory.toFlow(flowEntity);
                flow.setTemplateCode(flowTemplateCodeMap.get(flow.getFlowTemplateId()));
                List<Node> nodes = assembleNode(nodeMap.get(flowEntity.getId()), formEntities, fieldEntities,
                    postEntities, supplierEntities);
                flow.setNodes(nodes);
                return flow;
            })
            .collect(Collectors.toList());
    }

    private List<FieldEntity> parallelBatchQueryFieldEntities(List<Long> formIdList) {
        int batchSize = 50;
        List<List<Long>> parts = Lists.partition(formIdList, batchSize);
        List<Future<List<FieldEntity>>> futures = new ArrayList<>();
        for (List<Long> part : parts) {
            Future<List<FieldEntity>> futureFieldEntities = ThreadPoolFactory.THREAD_POOL_TODO_MAIN.submit(
                () -> fieldDao.selectList(Wrappers.<FieldEntity>lambdaQuery()
                    .in(FieldEntity::getFormId, part)));
            futures.add(futureFieldEntities);
        }
        List<FieldEntity> fieldEntities = new ArrayList<>();
        for (Future<List<FieldEntity>> future : futures) {
            List<FieldEntity> partOfFieldEntities = ThreadPoolFactory.get(future);
            if (partOfFieldEntities != null) {
                fieldEntities.addAll(partOfFieldEntities);
            }
        }
        return fieldEntities;
    }


    /**
     * 组装Node
     */
    private List<Node> assembleNode(List<NodeEntity> nodeEntities, List<FormEntity> formEntities,
        List<FieldEntity> fieldEntities, List<PostEntity> postEntities, List<SupplierEntity> supplierEntities) {
        List<Node> nodes = FlowEntityFactory.toNodes(nodeEntities);
        FlowEntityFactory.fillNodeForm(nodes, formEntities);
        // 填充字段信息
        FlowEntityFactory.fillNodeField(nodes, fieldEntities);
        // 填充岗位信息
        FlowEntityFactory.fillNodePost(nodes, postEntities);
        // 填充绑定的供应商信息
        FlowEntityFactory.fillNodeSupplier(nodes, supplierEntities);
        return nodes;
    }
}
