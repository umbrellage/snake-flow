package com.juliet.flow.repository.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.juliet.flow.client.common.thread.ThreadPoolFactory;
import com.juliet.flow.client.dto.ProcessConfigRPCDTO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.client.common.FlowStatusEnum;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.common.utils.JulietSqlUtil;
import com.juliet.flow.dao.*;
import com.juliet.flow.domain.entity.*;
import com.juliet.flow.domain.model.*;
import com.juliet.flow.domain.query.AssembleFlowCondition;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.repository.trasnfer.FlowEntityFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
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

    @Autowired
    private FlowCache flowCache;

    @Override
    public Long add(Flow flow) {
        FlowEntityFactory.cleanFlowId(flow);
        FlowEntity entity = FlowEntityFactory.toFlowEntity(flow);
        flowDao.insert(entity);
        flow.setId(entity.getId());
        addNodes(flow.getNodes(), entity.getId(), 0L);
        flowCache.removeFlow(entity.getId());
        Flow flowInDb = queryByIdFromDb(entity.getId());
        flowCache.setFlow(flowInDb);
        return entity.getId();
    }

    @Override
    public void refreshCache(Long flowId) {
        Flow flow = queryByIdFromDb(flowId);
        if (flow != null) {
            flowCache.setFlow(flow);
        }
    }

    private void addNodes(List<Node> nodes, Long flowId, Long flowTemplateId) {
        List<NodeEntity> nodeEntities = FlowEntityFactory.transferNodeEntities(nodes, flowId, flowTemplateId);
        nodeDao.insertBatch(nodeEntities);
        Map<Long, NodeEntity> nodeEntityMap = nodeEntities.stream().collect(Collectors.toMap(NodeEntity::getId, Function.identity(), (k1, k2) -> k1));
        nodes.forEach(node -> node.setId(nodeEntityMap.get(node.getId()).getId()));

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
    public Long addTemplate(FlowTemplate flowTemplate) {
        FlowTemplateEntity entity = FlowEntityFactory.toFlowTemplateEntity(flowTemplate);
        flowTemplateDao.insert(entity);
        fillUserId(flowTemplate.getNodes(), flowTemplate.getCreateBy(), flowTemplate.getUpdateBy());
        addNodes(flowTemplate.getNodes(), 0L, entity.getId());
        return entity.getId();
    }

    @Override
    public void update(Flow flow) {
        FlowEntity flowEntity = FlowEntityFactory.toFlowEntity(flow);
        flowDao.updateById(flowEntity);
        deleteNodes(flow.getNodes());
        addNodes(flow.getNodes(), flow.getId(), 0L);
        flowCache.removeFlow(flowEntity.getId());
        Flow flowInDb = queryByIdFromDb(flowEntity.getId());
        flowCache.setFlow(flowInDb);
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
        Flow cacheFlow = flowCache.getFlow(id);
        if (cacheFlow != null) {
            return cacheFlow;
        }
        Flow flow = queryByIdFromDb(id);
        if (flow != null) {
            flowCache.setFlow(flow);
        }
        return flow;
    }

    public Flow queryByIdFromDb(Long id) {
        StopWatch sw = new StopWatch("flow single query in db!");
        sw.start("start!");
        FlowEntity flowEntity = flowDao.selectById(id);
        if (flowEntity == null) {
            return null;
        }
        List<NodeEntity> nodeEntities = nodeDao.selectList(Wrappers.<NodeEntity>lambdaQuery()
                .eq(NodeEntity::getFlowId, id));
        List<Node> nodes = assembleNode(nodeEntities);
        Flow flow = FlowEntityFactory.toFlow(flowEntity);
        FlowTemplateEntity flowTemplateEntity = flowTemplateDao.selectById(flowEntity.getFlowTemplateId());
        flow.setNodes(nodes);
        flow.setTemplateCode(flowTemplateEntity.getCode());
        sw.stop();
        log.info(sw.prettyPrint());
        return flow;
    }

    @Override
    public List<Flow> queryByIdList(List<Long> idList) {
        return queryByIdList(idList, new AssembleFlowCondition());
    }

    @Override
    public List<Flow> queryByIdList(List<Long> idList, AssembleFlowCondition condition) {
        List<Flow> flows = new ArrayList<>();
        if (CollectionUtils.isEmpty(idList)) {
            return flows;
        }
        FlowCache.FlowCacheData flowCacheData = flowCache.getFlowList(idList);
        if (!CollectionUtils.isEmpty(flowCacheData.getFlowList())) {
            flows.addAll(flowCacheData.getFlowList());
        }
        if (CollectionUtils.isNotEmpty(flowCacheData.getMissKeyList())) {
            List<Flow> flowsInDb =  queryByIdListFromDb(flowCacheData.getMissKeyList(), new AssembleFlowCondition());
            if (CollectionUtils.isNotEmpty(flowsInDb)) {
                flows.addAll(flowsInDb);
                flowCache.setFlowList(flowsInDb);
            }
        }
        return flows;
    }

    private List<Flow> queryByIdListFromDb(List<Long> idList, AssembleFlowCondition condition) {
        List<FlowEntity> flowList = flowDao.selectList(
                Wrappers.<FlowEntity>lambdaQuery().in(FlowEntity::getId, idList));
        return assembleFlow(flowList, condition);
    }

    @Override
    public List<Flow> listFlowByParentId(Long id) {
        List<FlowEntity> flowEntities = flowDao.selectList(Wrappers.<FlowEntity>lambdaQuery()
                .eq(FlowEntity::getParentId, id));
        if (CollectionUtils.isEmpty(flowEntities)) {
            return Lists.newArrayList();
        }
        List<Long> flowIdList = flowEntities.stream().map(FlowEntity::getId).collect(Collectors.toList());
        return queryByIdList(flowIdList, AssembleFlowCondition.noExcludeFields());
    }

    @Override
    public List<Flow> listFlowByParentId(Collection<Long> idList) {
        return listFlowByParentId(idList, new AssembleFlowCondition());
    }

    @Override
    public List<Flow> listFlowByParentId(Collection<Long> idList, AssembleFlowCondition condition) {
        List<Flow> flowList = new ArrayList<>();
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.emptyList();
        }
        List<FlowEntity> flowEntities = flowDao.selectList(Wrappers.<FlowEntity>lambdaQuery()
                .in(FlowEntity::getParentId, idList));
        if (CollectionUtils.isEmpty(flowEntities)) {
            return Lists.newArrayList();
        }
        List<Long> flowIdList = flowEntities.stream().map(FlowEntity::getId).collect(Collectors.toList());
        FlowCache.FlowCacheData flowCacheData = flowCache.getFlowList(flowIdList);
        if (CollectionUtils.isNotEmpty(flowCacheData.getFlowList())) {
            flowList.addAll(flowCacheData.getFlowList());
        }
        if (CollectionUtils.isNotEmpty(flowCacheData.getMissKeyList())) {
            List<Flow> flowListInDb = assembleFlow(flowEntities.stream()
                            .filter(flowEntity -> flowCacheData.getMissKeyList().contains(flowEntity.getId())).collect(Collectors.toList()),
                    condition);
            flowCache.setFlowList(flowListInDb);
            flowList.addAll(flowListInDb);
        }
        return flowList;
    }

    @Override
    public List<Flow> queryMainFlowById(Collection<Long> idList) {
        List<FlowEntity> flowEntities = flowDao.selectList(Wrappers.<FlowEntity>lambdaQuery()
                .in(FlowEntity::getId, idList)
                .eq(FlowEntity::getParentId, 0)
        );
        return assembleFlow(flowEntities, new AssembleFlowCondition());
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
    public FlowTemplate queryTemplateByCode(String code) {
        FlowTemplateEntity flowTemplateEntity = flowTemplateDao.selectOne(Wrappers.<FlowTemplateEntity>lambdaQuery()
                .eq(FlowTemplateEntity::getCode, code)
                .last("limit 1"));
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
//                        .orderByDesc(FlowTemplateEntity::getUpdateTime)
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

    @Override
    public List<Flow> listFlow(String flowCode, Long userId, List<Long> postIdList) {
        if (StringUtils.isBlank(flowCode) || userId == null) {
            log.error("流程code或者用户id必须不能为空");
            return Collections.emptyList();
        }
        List<FlowTemplateEntity> flowTemplateEntityList = flowTemplateDao.selectList(
            Wrappers.<FlowTemplateEntity>lambdaQuery()
                .eq(FlowTemplateEntity::getCode, flowCode));
        if (CollectionUtils.isEmpty(flowTemplateEntityList)) {
            log.error("找不到流程模版");
            return Collections.emptyList();
        }
        List<Long> templateIdList = flowTemplateEntityList.stream()
            .map(FlowTemplateEntity::getId)
            .collect(Collectors.toList());

        List<FlowEntity> flowEntityList = flowDao.selectList(Wrappers.<FlowEntity>lambdaQuery()
            .in(FlowEntity::getFlowTemplateId, templateIdList));
        if (CollectionUtils.isEmpty(flowEntityList)) {
            return Collections.emptyList();
        }
        List<Long> flowIdList = flowEntityList.stream()
            .map(FlowEntity::getId)
            .collect(Collectors.toList());
        List<Flow> flowList = queryByIdList(flowIdList);
        /*
         * 这里的operatorOfUserIdList里包含了异常流程和主流程，需要过滤一下
         * 这些数据里有异常流程不一定有主流程，为什么呢，因为变更的原因但是分支节点本来走A不走B，现在走B不走A，会进行流程校准
         */
        List<Flow> operatorOfUserIdList = flowList.stream()
            .filter(flow -> flow.isFlowOperator(userId, postIdList))
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(operatorOfUserIdList)) {
            return Collections.emptyList();
        }

        // 这里是要过滤出主流程id
        List<Long> operatorOfUserIdMainFlowIdList = operatorOfUserIdList.stream()
            .map(flow -> {
                if (flow.hasParentFlow()) {
                    return flow.getParentId();
                }
                return flow.getId();
            })
            .collect(Collectors.toList());
        return queryByIdList(operatorOfUserIdMainFlowIdList);
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
//        List<Long> supplierDataIdList = nodes.stream()
//            .map(Node::getBindSuppliers)
//            .filter(CollectionUtils::isNotEmpty)
//            .flatMap(Collection::stream)
//            .filter(Objects::nonNull)
//            .map(Supplier::getId)
//            .filter(Objects::nonNull)
//            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(nodeIds)){
            supplierDao.delete(Wrappers.<SupplierEntity>lambdaUpdate().in(SupplierEntity::getNodeId, nodeIds));
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
        List<SupplierEntity> supplierEntities = supplierDao.selectList(Wrappers.<SupplierEntity>lambdaQuery()
                .in(SupplierEntity::getNodeId, nodeIds));
        FlowEntityFactory.fillNodeSupplier(nodes, supplierEntities);
        return nodes;
    }

    public List<Flow> assembleFlow(List<FlowEntity> flowList, AssembleFlowCondition condition) {
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

        Future<List<PostEntity>> futurePostEntities = null;
        if (!Boolean.TRUE.equals(condition.getExcludePost())) {
            futurePostEntities = ThreadPoolFactory.THREAD_POOL_TODO_MAIN.submit(
                    () -> postDao.selectList(Wrappers.<PostEntity>lambdaQuery()
                            .in(PostEntity::getNodeId, nodeIdList)));
        }

        Future<List<SupplierEntity>> futureSupplierEntities = null;
        if (!Boolean.TRUE.equals(condition.getExcludeSupplier())) {
            futureSupplierEntities = ThreadPoolFactory.THREAD_POOL_TODO_MAIN.submit(
                    () -> supplierDao.selectList(Wrappers.<SupplierEntity>lambdaQuery()
                            .in(SupplierEntity::getNodeId, nodeIdList)));
        }

        Future<List<FormEntity>> futureFormEntities = null;
        if (!Boolean.TRUE.equals(condition.getExcludeForm())) {
            futureFormEntities = ThreadPoolFactory.THREAD_POOL_TODO_MAIN.submit(
                    () -> formDao.selectList(Wrappers.<FormEntity>lambdaQuery()
                            .in(FormEntity::getNodeId, nodeIdList)));
        }

        List<PostEntity> postEntities = ThreadPoolFactory.get(futurePostEntities);
        List<SupplierEntity> supplierEntities = ThreadPoolFactory.get(futureSupplierEntities);
        List<FormEntity> formEntities = ThreadPoolFactory.get(futureFormEntities);
        List<FieldEntity> fieldEntities;
        if (CollectionUtils.isNotEmpty(formEntities) && !Boolean.TRUE.equals(condition.getExcludeFields())) {
            List<Long> formIdList = formEntities.stream().map(FormEntity::getId).distinct().collect(Collectors.toList());
            fieldEntities = parallelBatchQueryFieldEntities(formIdList);
        } else {
            fieldEntities = null;
        }
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
