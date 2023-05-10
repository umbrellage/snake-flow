package com.juliet.flow.repository.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.dao.*;
import com.juliet.flow.domain.entity.*;
import com.juliet.flow.domain.model.*;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.repository.trasnfer.FlowEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
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

    @Transactional
    @Override
    public void add(Flow flow) {
        FlowEntity entity = FlowEntityFactory.toFlowEntity(flow);
        flowDao.insert(entity);
        addNodes(flow.getNode(), flow.getTenantId(), flow.getId(), 0L);
    }

    private void addNodes(Node node, Long tenantId, Long flowId, Long flowTemplateId) {
        List<NodeEntity> nodeEntities = new ArrayList<>();
        FlowEntityFactory.transferNodeEntities(nodeEntities, node, tenantId, 0L, flowId, flowTemplateId);
        nodeEntities.forEach(nodeEntity -> nodeDao.insert(nodeEntity));

        List<FormEntity> formEntities = new ArrayList<>();
        FlowEntityFactory.transferFormEntities(formEntities, node, tenantId);
        // TODO 改批量
        formEntities.forEach(formEntity -> formDao.insert(formEntity));

        List<FieldEntity> fieldEntities = new ArrayList<>();
        FlowEntityFactory.transferFieldEntities(fieldEntities, node, tenantId);
        // TODO 改批量
        fieldEntities.forEach(fieldEntity -> fieldDao.insert(fieldEntity));

        List<PostEntity> postEntities = new ArrayList<>();
        FlowEntityFactory.transferPostEntity(postEntities, node, tenantId);
        // TODO 改批量
        postEntities.forEach(postEntity -> postDao.insert(postEntity));
    }

    @Override
    public void addTemplate(FlowTemplate flowTemplate) {
        FlowTemplateEntity entity = FlowEntityFactory.toFlowTemplateEntity(flowTemplate);
        flowTemplateDao.insert(entity);
        addNodes(flowTemplate.getNode(), flowTemplate.getTenantId(), 0L, flowTemplate.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(Flow flow) {
        FlowEntity flowEntity = FlowEntityFactory.toFlowEntity(flow);
        flowDao.updateById(flowEntity);
        deleteNodes(flow.getNode());
        addNodes(flow.getNode(), flow.getTenantId(), flow.getId(), 0L);
    }

    @Override
    public Flow queryById(Long id) {
        FlowEntity flowEntity = flowDao.selectById(id);
        Flow flow = FlowEntityFactory.toFlow(flowEntity);
        Node node = getStartNode(flowEntity.getId());
        flow.setNode(node);
        return flow;
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

    }

    @Override
    public FlowTemplate queryTemplateById(Long id) {
        FlowTemplateEntity flowTemplateEntity = flowTemplateDao.selectById(id);
        FlowTemplate flowTemplate = FlowEntityFactory.toFlowTemplate(flowTemplateEntity);
        flowTemplate.setNode(getTemplateStartNode(flowTemplateEntity.getId()));
        return flowTemplate;
    }

    @Override
    public void updateFlowTemplateStatusById(FlowTemplateStatusEnum status, Long id) {
        flowTemplateDao.selectById(id);
    }

    private void deleteNodes(Node node) {
        if (node == null) {
            return;
        }
        List<Long> nodeIds = new ArrayList<>();
        FlowEntityFactory.getAllNodeId(nodeIds, node);
        if (!CollectionUtils.isEmpty(nodeIds)) {
            nodeDao.delete(Wrappers.<NodeEntity>lambdaUpdate()
                    .in(NodeEntity::getId, nodeIds));
            postDao.delete(Wrappers.<PostEntity>lambdaUpdate()
                    .in(PostEntity::getNodeId, nodeIds));
        }

        List<Long> formIds = new ArrayList<>();
        FlowEntityFactory.getAllFormId(formIds, node);
        if (!CollectionUtils.isEmpty(formIds)) {
            fieldDao.delete(Wrappers.<FieldEntity>lambdaQuery()
                    .in(FieldEntity::getFormId, formIds));
        }
    }

    /**
     * 获取流程模板的开始节点
     */
    private Node getTemplateStartNode(Long templateFlowId) {
        List<NodeEntity> nodeEntities = nodeDao.selectList(Wrappers.<NodeEntity>lambdaUpdate()
                .eq(NodeEntity::getFlowTemplateId, templateFlowId));
        return assembleNode(nodeEntities);
    }

    /**
     * 获取某个流程的开始节点
     */
    private Node getStartNode(Long flowId) {
        List<NodeEntity> nodeEntities = nodeDao.selectList(Wrappers.<NodeEntity>lambdaQuery()
                .eq(NodeEntity::getFlowId, flowId));
        return assembleNode(nodeEntities);
    }

    /**
     * 组装Node
     */
    private Node assembleNode(List<NodeEntity> nodeEntities) {
        Node node = FlowEntityFactory.toNode(nodeEntities);
        if (node == null) {
            return null;
        }
        // 填充表单信息
        List<Long> ids = nodeEntities.stream().map(NodeEntity::getId).collect(Collectors.toList());
        List<FormEntity> formEntities = formDao.selectList(Wrappers.<FormEntity>lambdaQuery()
                .in(FormEntity::getId, ids));
        FlowEntityFactory.fillNodeForm(node, formEntities);
        // 填充字段信息
        List<FieldEntity> fieldEntities = fieldDao.selectList(Wrappers.<FieldEntity>lambdaQuery()
                .in(FieldEntity::getFormId, formEntities.stream().map(FormEntity::getId).distinct().collect(Collectors.toList())));
        FlowEntityFactory.fillNodeField(node, fieldEntities);
        // 填充岗位信息
        List<PostEntity> postEntities = postDao.selectList(Wrappers.<PostEntity>lambdaQuery()
                .in(PostEntity::getNodeId, ids));
        FlowEntityFactory.fillNodePost(node, postEntities);
        return node;
    }
}
