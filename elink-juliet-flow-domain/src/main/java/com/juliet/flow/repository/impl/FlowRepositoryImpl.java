package com.juliet.flow.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.dao.*;
import com.juliet.flow.domain.entity.*;
import com.juliet.flow.domain.model.*;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.repository.trasnfer.FlowEntityFactory;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Repository
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
    private FlowTemplateDao flowTemplateDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(Flow flow) {
        FlowEntityFactory.cleanFlowId(flow);
        FlowEntity entity = FlowEntityFactory.toFlowEntity(flow);
        flowDao.insert(entity);
        addNodes(flow.getNodes(), flow.getTenantId(), entity.getId(), 0L);
    }

    private void addNodes(List<Node> nodes, Long tenantId, Long flowId, Long flowTemplateId) {
        List<NodeEntity> nodeEntities = FlowEntityFactory.transferNodeEntities(nodes,
            tenantId, flowId, flowTemplateId);
        nodeEntities.forEach(nodeEntity -> nodeDao.insert(nodeEntity));

        List<FormEntity> formEntities = FlowEntityFactory.transferFormEntities(nodes, tenantId);
        // TODO 改批量
        formEntities.forEach(formEntity -> formDao.insert(formEntity));

        List<FieldEntity> fieldEntities = FlowEntityFactory.transferFieldEntities(nodes, tenantId);
        // TODO 改批量
        fieldEntities.forEach(fieldEntity -> fieldDao.insert(fieldEntity));

        List<PostEntity> postEntities = FlowEntityFactory.transferPostEntity(nodes, tenantId);
        // TODO 改批量
        postEntities.forEach(postEntity -> postDao.insert(postEntity));
    }

    @Override
    public void addTemplate(FlowTemplate flowTemplate) {
        FlowTemplateEntity entity = FlowEntityFactory.toFlowTemplateEntity(flowTemplate);
        flowTemplateDao.insert(entity);
        addNodes(flowTemplate.getNodes(), flowTemplate.getTenantId(), 0L, entity.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(Flow flow) {
        FlowEntity flowEntity = FlowEntityFactory.toFlowEntity(flow);
        flowDao.updateById(flowEntity);
        deleteNodes(flow.getNodes());
        addNodes(flow.getNodes(), flow.getTenantId(), flow.getId(), 0L);
    }

    @Override
    public void updateTemplate(FlowTemplate flowTemplate) {
        FlowTemplateEntity flowTemplateEntity = FlowEntityFactory.toFlowTemplateEntity(flowTemplate);
        FlowTemplate flowTemplateOld = queryTemplateById(flowTemplate.getId());
        BusinessAssert.assertNotNull(flowTemplateOld, StatusCode.ILLEGAL_PARAMS, "找不到模板，id：" + flowTemplate.getId());
        flowTemplateDao.updateById(flowTemplateEntity);
        deleteNodes(flowTemplateOld.getNodes());
        addNodes(flowTemplate.getNodes(), flowTemplate.getTenantId(), 0L, flowTemplate.getId());
    }

    @Override
    public Flow queryById(Long id) {
        FlowEntity flowEntity = flowDao.selectById(id);
        if (flowEntity == null) {
            return null;
        }
        Flow flow = FlowEntityFactory.toFlow(flowEntity);
        List<Node> nodes = getNodes(flowEntity.getId());
        flow.setNodes(nodes);
        return flow;
    }

    @Override
    public List<Flow> listFlowByParentId(Long id) {
        List<FlowEntity> flowEntities = flowDao.selectList(Wrappers.<FlowEntity>lambdaQuery()
            .eq(FlowEntity::getParentId, id));
        if (CollectionUtils.isEmpty(flowEntities)) {
            return Lists.newArrayList();
        }
        // 不会很多
        List<Flow> flows = flowEntities.stream().map(FlowEntityFactory::toFlow).collect(Collectors.toList());
        flows.forEach(flow -> {
            List<Node> nodes = getNodes(flow.getId());
            flow.setNodes(nodes);
        });
        return flows;
    }

    @Override
    public Flow queryByCode(String code) {
        FlowEntity flowEntity = flowDao.selectOne(Wrappers.<FlowEntity>lambdaQuery()
            .eq(FlowEntity::getId, code)
            .last("limit 1"));
        return FlowEntityFactory.toFlow(flowEntity);
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
        FlowTemplateEntity flowTemplateEntity = flowTemplateDao.selectOne(
            Wrappers.<FlowTemplateEntity>lambdaQuery().eq(FlowTemplateEntity::getCode, code)
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
        if (nodes == null) {
            return null;
        }
        // 填充表单信息
        List<Long> nodeIds = nodeEntities.stream().map(NodeEntity::getId).collect(Collectors.toList());
        List<FormEntity> formEntities = formDao.selectList(Wrappers.<FormEntity>lambdaQuery()
            .in(FormEntity::getNodeId, nodeIds));
        FlowEntityFactory.fillNodeForm(nodes, formEntities);
        // 填充字段信息
        List<FieldEntity> fieldEntities = fieldDao.selectList(Wrappers.<FieldEntity>lambdaQuery()
            .in(FieldEntity::getFormId,
                formEntities.stream().map(FormEntity::getId).distinct().collect(Collectors.toList())));
        FlowEntityFactory.fillNodeField(nodes, fieldEntities);
        // 填充岗位信息
        List<PostEntity> postEntities = postDao.selectList(Wrappers.<PostEntity>lambdaQuery()
            .in(PostEntity::getNodeId, nodeIds));
        FlowEntityFactory.fillNodePost(nodes, postEntities);
        return nodes;
    }
}
