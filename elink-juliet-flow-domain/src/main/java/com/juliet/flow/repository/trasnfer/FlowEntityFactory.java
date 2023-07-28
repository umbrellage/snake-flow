package com.juliet.flow.repository.trasnfer;

import com.juliet.common.core.utils.StringUtils;
import com.juliet.common.core.utils.time.JulietTimeMemo;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import com.juliet.flow.common.utils.IdGenerator;
import com.juliet.flow.domain.entity.*;
import com.juliet.flow.domain.model.*;

import java.util.*;

import com.juliet.flow.domain.model.rule.RuleFactory;
import org.apache.commons.compress.utils.Lists;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
public class FlowEntityFactory {

    public static Flow toFlow(FlowEntity flowEntity, FlowTemplateEntity template) {
        Flow flow = new Flow();
        flow.setName(flowEntity.getName());
        flow.setTenantId(flowEntity.getTenantId());
        flow.setTemplateCode(template.getCode());
        flow.setId(flowEntity.getId());
        flow.setParentId(flowEntity.getParentId());
        flow.setStatus(FlowStatusEnum.findByCode(flowEntity.getStatus()));
        flow.setFlowTemplateId(flowEntity.getFlowTemplateId());
        flow.setCreateTime(flowEntity.getCreateTime());
        flow.setUpdateTime(flowEntity.getUpdateTime());
        return flow;
    }

    public static void cleanFlowId(Flow flow) {
        flow.setId(null);
        if (!CollectionUtils.isEmpty(flow.getNodes())) {
            flow.getNodes().forEach(node -> {
                node.setId(null);
                if (node.getForm() != null) {
                    node.getForm().setId(null);
                    if (!CollectionUtils.isEmpty(node.getForm().getFields())) {
                        for (Field field : node.getForm().getFields()) {
                            field.setId(null);
                        }
                    }
                }
                if (!CollectionUtils.isEmpty(node.getBindPosts())) {
                    for (Post bindPost : node.getBindPosts()) {
                        bindPost.setId(null);
                    }
                }
            });
        }
    }


    public static FlowEntity toFlowEntity(Flow flow) {
        FlowEntity flowEntity = new FlowEntity();
        if (flow.getId() == null) {
            flowEntity.setId(IdGenerator.getId());
        } else {
            flowEntity.setId(flow.getId());
        }
        flowEntity.setName(flow.getName());
        flowEntity.setParentId(flow.getParentId());
        flowEntity.setFlowTemplateId(flow.getFlowTemplateId());
        Optional.ofNullable(flow.getStatus()).ifPresent(status -> flowEntity.setStatus(status.getCode()));
        flowEntity.setCreateBy(flow.getCreateBy());
        flowEntity.setUpdateBy(flow.getUpdateBy());
        flowEntity.setTenantId(flow.getTenantId());
        return flowEntity;
    }

    public static FlowTemplateEntity toFlowTemplateEntity(FlowTemplate flowTemplate) {
        FlowTemplateEntity flowTemplateEntity = new FlowTemplateEntity();
        flowTemplateEntity.setId(flowTemplate.getId() == null ? IdGenerator.getId() : flowTemplate.getId());
        flowTemplateEntity.setName(flowTemplate.getName());
        flowTemplateEntity.setCode(flowTemplate.getCode());
        flowTemplateEntity.setTenantId(flowTemplate.getTenantId());
//        if (flowTemplate.getNode() != null) {
//            Node node = flowTemplate.getNode();
//            if (node.getId() == null) {
//                node.setId(IdGenerator.getId());
//            }
//        }
        flowTemplateEntity.setCreateBy(flowTemplate.getCreateBy());
        flowTemplateEntity.setUpdateBy(flowTemplate.getUpdateBy());
        flowTemplateEntity.setCode(flowTemplate.getCode());
        flowTemplateEntity.setStatus(flowTemplate.getStatus().getCode());
        return flowTemplateEntity;
    }

    public static List<FormEntity> transferFormEntities(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return Lists.newArrayList();
        }
        List<FormEntity> formEntities = new ArrayList<>();
        for (Node node : nodes) {
            FormEntity formEntity = toFormEntity(node.getForm(), node.getTenantId(), node.getId());
            if (formEntity != null) {
                formEntities.add(formEntity);
            }
        }
        return formEntities;
    }

    private static FormEntity toFormEntity(Form form, Long tenantId, Long nodeId) {
        if (form == null) {
            return null;
        }
        if (form.getCode() == null) {
            return null;
        }
        FormEntity entity = new FormEntity();
        if (form.getId() == null) {
            form.setId(IdGenerator.getId());
        }
        entity.setId(form.getId());
        entity.setNodeId(nodeId);
        entity.setName(form.getName());
        entity.setCode(form.getCode());
        entity.setPath(form.getPath());
        entity.setTenantId(tenantId);
        entity.setCreateBy(form.getCreateBy());
        entity.setUpdateBy(form.getUpdateBy());
        entity.setStatus(0);
        Date now = new Date();
        entity.setCreateTime(form.getCreateTime() == null ? now : form.getCreateTime());
        entity.setUpdateTime(form.getUpdateTime() == null ? now : form.getUpdateTime());
        return entity;
    }

    public static List<FieldEntity> transferFieldEntities(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return Lists.newArrayList();
        }
        List<FieldEntity> fieldEntities = new ArrayList<>();
        for (Node node : nodes) {
            if (node.getForm() != null && !CollectionUtils.isEmpty(node.getForm().getFields())) {
                for (Field field : node.getForm().getFields()) {
                    FieldEntity entity = toFieldEntity(field, node.getTenantId(), node.getForm().getId());
                    fieldEntities.add(entity);
                }
            }
        }
        return fieldEntities;
    }

    private static FieldEntity toFieldEntity(Field field, Long tenantId, Long formId) {
        FieldEntity entity = new FieldEntity();
        if (field.getId() == null) {
            field.setId(IdGenerator.getId());
        }
        entity.setId(field.getId());
        entity.setFormId(formId);
        entity.setTenantId(tenantId);
        entity.setName(field.getName());
        entity.setCode(field.getCode());
        entity.setDelFlag(0);
        Date now = new Date();
        entity.setCreateTime(field.getCreateTime() == null ? now : field.getCreateTime());
        entity.setUpdateTime(field.getUpdateTime() == null ? now : field.getUpdateTime());
        entity.setCreateBy(field.getCreateBy());
        entity.setUpdateBy(field.getUpdateBy());
        return entity;
    }

    public static List<PostEntity> transferPostEntity(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return Lists.newArrayList();
        }
        List<PostEntity> postEntities = new ArrayList<>();
        for (Node node : nodes) {
            if (!CollectionUtils.isEmpty(node.getBindPosts())) {
                for (Post post : node.getBindPosts()) {
                    postEntities.add(toPostEntity(post, node.getTenantId(), node.getId()));
                }
            }
        }
        return postEntities;
    }

    private static PostEntity toPostEntity(Post post, Long tenantId, Long nodeId) {
        PostEntity entity = new PostEntity();
        if (post.getId() == null) {
            post.setId(IdGenerator.getId());
        }
        entity.setId(post.getId());
        entity.setPostId(post.getPostId());
        entity.setPostName(post.getPostName());
        entity.setNodeId(nodeId);
        entity.setTenantId(tenantId);
        entity.setDelFlag(0);
        Date now = new Date();
        entity.setCreateTime(post.getCreateTime() == null ? now : post.getCreateTime());
        entity.setUpdateTime(post.getUpdateTime() == null ? now : post.getUpdateTime());
        entity.setCreateBy(post.getCreateBy());
        entity.setUpdateBy(post.getUpdateBy());
        return entity;
    }

    public static List<SupplierEntity> transferSupplierEntity(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return Lists.newArrayList();
        }
        List<SupplierEntity> supplierEntities = new ArrayList<>();
        for (Node node : nodes) {
            if (!CollectionUtils.isEmpty(node.getBindSuppliers())) {
                for (Supplier supplier : node.getBindSuppliers()) {
                    supplierEntities.add(toSupplierEntity(supplier, node.getTenantId(), node.getId()));
                }
            }
        }
        return supplierEntities;
    }

    private static SupplierEntity toSupplierEntity(Supplier supplier, Long tenantId, Long nodeId) {
        SupplierEntity entity = new SupplierEntity();
        if (supplier.getId() == null) {
            supplier.setId(IdGenerator.getId());
        }
        entity.setId(supplier.getId());
        entity.setSupplierType(supplier.getSupplierType());
        entity.setNodeId(nodeId);
        entity.setTenantId(tenantId);
        entity.setDelFlag(0);
        Date now = new Date();
        entity.setCreateTime(supplier.getCreateTime() == null ? now : supplier.getCreateTime());
        entity.setUpdateTime(supplier.getUpdateTime() == null ? now : supplier.getUpdateTime());
        entity.setCreateBy(supplier.getCreateBy());
        entity.setUpdateBy(supplier.getUpdateBy());
        return entity;
    }

    public static List<NodeEntity> transferNodeEntities(List<Node> nodes,
        Long flowId,
        Long flowTemplateId) {
        if (CollectionUtils.isEmpty(nodes)) {
            return Lists.newArrayList();
        }

        return nodes.stream()
            .map(node -> toNodeEntity(node, flowId, flowTemplateId))
            .collect(Collectors.toList());
    }

    private static NodeEntity toNodeEntity(Node node, Long flowId, Long flowTemplateId) {
        NodeEntity nodeEntity = new NodeEntity();
        if (node.getId() == null) {
            node.setId(IdGenerator.getId());
        }
        nodeEntity.setId(node.getId());
        nodeEntity.setTitle(node.getTitle());
        nodeEntity.setName(node.getName());
        nodeEntity.setPreName(node.getPreName());
        nodeEntity.setNextName(node.getNextName());
        nodeEntity.setTenantId(node.getTenantId());
        nodeEntity.setFlowId(flowId);
        nodeEntity.setFlowTemplateId(flowTemplateId);

        nodeEntity.setAccessRuleName(node.getAccessRule() == null ? "" : "1");
        nodeEntity.setSubmitRuleName("");
        nodeEntity.setAssignRuleName(node.getAssignRule() == null ? "" : node.getAssignRule().getRuleName());
        nodeEntity.setSupervisorAssignment(node.getSupervisorAssignment() != null && node.getSupervisorAssignment() ? 1 : 0);
        nodeEntity.setSelfAndSupervisorAssignment(node.getSelfAndSupervisorAssignment() != null && node.getSelfAndSupervisorAssignment() ? 1 : 0);
        nodeEntity.setRuleAssignment(node.getRuleAssignment() != null && node.getRuleAssignment() ? 1 : 0);
        nodeEntity.setSupervisorIds(node.supervisorIds());

        nodeEntity.setType(node.getType().getCode());
        nodeEntity.setStatus(node.getStatus().getCode());
        nodeEntity.setCustomStatus(node.getCustomStatus() == null ? "" : node.getCustomStatus());
        nodeEntity.setProcessedBy(node.getProcessedBy() == null ? 0 : node.getProcessedBy());
        nodeEntity.setDelFlag(0);
        nodeEntity.setCreateBy(node.getCreateBy());
        nodeEntity.setUpdateBy(node.getUpdateBy());
        Date now = new Date();
        nodeEntity.setCreateTime(node.getCreateTime() == null ? now : node.getCreateTime());
        nodeEntity.setUpdateTime(node.getUpdateTime() == null ? now : node.getUpdateTime());
        return nodeEntity;
    }

    public static FlowTemplate toFlowTemplate(FlowTemplateEntity flowTemplateEntity) {
        FlowTemplate flowTemplate = new FlowTemplate();
        flowTemplate.setId(flowTemplateEntity.getId());
        flowTemplate.setName(flowTemplateEntity.getName());
        flowTemplate.setCode(flowTemplateEntity.getCode());
        flowTemplate.setStatus(FlowTemplateStatusEnum.byCode(flowTemplateEntity.getStatus()));
        flowTemplate.setTenantId(flowTemplateEntity.getTenantId());
        flowTemplate.setCreateBy(flowTemplateEntity.getCreateBy());
        flowTemplate.setUpdateBy(flowTemplateEntity.getUpdateBy());
        return flowTemplate;
    }

    public static List<Node> toNodes(List<NodeEntity> nodeEntities) {
        if (CollectionUtils.isEmpty(nodeEntities)) {
            return Lists.newArrayList();
        }
        return nodeEntities.stream().map(FlowEntityFactory::toSingleNode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static Node toSingleNode(NodeEntity nodeEntity) {
        if (nodeEntity == null) {
            return null;
        }
        Node node = new Node();
        node.setId(nodeEntity.getId());
        node.setFlowId(nodeEntity.getFlowId());
        node.setTitle(nodeEntity.getTitle());
        node.setName(nodeEntity.getName());
        node.setPreName(nodeEntity.getPreName());
        node.setNextName(nodeEntity.getNextName());

        node.setSupervisorAssignment(nodeEntity.getSupervisorAssignment().intValue() == 1);
        node.setSelfAndSupervisorAssignment(nodeEntity.getSelfAndSupervisorAssignment().intValue() == 1);
        node.setRuleAssignment(nodeEntity.getRuleAssignment().intValue() == 1);
        node.setAccessRule(RuleFactory.getAccessRule(nodeEntity.getAccessRuleName()));
        node.setAssignRule(RuleFactory.getAssignRule(nodeEntity.getAssignRuleName()));
        if (StringUtils.isNotBlank(nodeEntity.getSupervisorIds())) {
            node.setSupervisorIds(nodeEntity.supervisorIds());
        } else {
            node.setSupervisorIds(Lists.newArrayList());
        }
        node.setStatus(NodeStatusEnum.byCode(nodeEntity.getStatus()));
        node.setCustomStatus(nodeEntity.getCustomStatus());
        node.setType(NodeTypeEnum.byCode(nodeEntity.getType()));
        node.setProcessedBy(nodeEntity.getProcessedBy());
        node.setCreateBy(nodeEntity.getCreateBy());
        node.setUpdateBy(nodeEntity.getUpdateBy());
        node.setTenantId(nodeEntity.getTenantId());
        node.setProcessedTime(JulietTimeMemo.toDateTime(nodeEntity.getUpdateTime()));
        node.setCreateTime(nodeEntity.getCreateTime());
        node.setUpdateTime(nodeEntity.getUpdateTime());
        return node;
    }

    public static void fillNodeField(List<Node> nodes, List<FieldEntity> fieldEntities) {
        if (CollectionUtils.isEmpty(fieldEntities)) {
            return;
        }
        for (Node node : nodes) {
            if (node != null && node.getForm() != null) {
                List<FieldEntity> matchedFieldEntities = fieldEntities.stream()
                    .filter(fieldEntity -> fieldEntity.getFormId().equals(node.getForm().getId()))
                    .collect(Collectors.toList());
                node.getForm().setFields(
                    matchedFieldEntities.stream().map(FlowEntityFactory::toField).collect(Collectors.toList()));
            }
        }
    }

    public static Field toField(FieldEntity fieldEntity) {
        Field field = new Field();
        field.setId(fieldEntity.getId());
        field.setName(fieldEntity.getName());
        field.setCode(fieldEntity.getCode());
        field.setCreateBy(fieldEntity.getCreateBy());
        field.setUpdateBy(fieldEntity.getUpdateBy());
        field.setTenantId(fieldEntity.getTenantId());
        return field;
    }

    public static void fillNodeForm(List<Node> nodes, List<FormEntity> formEntities) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            List<FormEntity> matchFormEntities = formEntities.stream()
                .filter(formEntity -> formEntity.getNodeId().equals(node.getId()))
                .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(matchFormEntities)) {
                node.setForm(toForm(matchFormEntities.get(0)));
            }
        }
    }

    private static Form toForm(FormEntity formEntity) {
        Form form = new Form();
        form.setId(formEntity.getId());
        form.setName(formEntity.getName());
        form.setCode(formEntity.getCode());
        form.setPath(formEntity.getPath());
        form.setCreateBy(formEntity.getCreateBy());
        form.setUpdateBy(formEntity.getUpdateBy());
        form.setTenantId(formEntity.getTenantId());
        return form;
    }

    public static void fillNodePost(List<Node> nodes, List<PostEntity> postEntities) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            List<PostEntity> bindPostEntities = postEntities.stream()
                .filter(postEntity -> postEntity.getNodeId().equals(node.getId()))
                .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(bindPostEntities)) {
                node.setBindPosts(
                    bindPostEntities.stream().map(FlowEntityFactory::toPost).collect(Collectors.toList()));
            }
        }
    }

    private static Post toPost(PostEntity postEntity) {
        Post post = new Post();
        post.setId(postEntity.getId());
        post.setPostId(postEntity.getPostId());
        post.setPostName(postEntity.getPostName());
        post.setCreateBy(postEntity.getCreateBy());
        post.setUpdateBy(postEntity.getUpdateBy());
        post.setTenantId(postEntity.getTenantId());
        return post;
    }
}
