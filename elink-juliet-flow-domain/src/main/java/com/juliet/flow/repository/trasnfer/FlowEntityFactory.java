package com.juliet.flow.repository.trasnfer;

import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.common.utils.IdGenerator;
import com.juliet.flow.domain.entity.*;
import com.juliet.flow.domain.model.*;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
public class FlowEntityFactory {

    public static Flow toFlow(FlowEntity flowEntity) {
        Flow flow = new Flow();
        flow.setId(flowEntity.getId());
        return flow;
    }

    public static FlowEntity toFlowEntity(Flow flow) {
        FlowEntity flowEntity = new FlowEntity();
        flowEntity.setId(flow.getId());
        flowEntity.setName(flow.getName());
        flowEntity.setParentId(flow.getParentId());
        flowEntity.setTemplateId(flow.getTemplateId());
        flowEntity.setStatus(flow.getStatus().getCode());
        return flowEntity;
    }

    public static FlowTemplateEntity toFlowTemplateEntity(FlowTemplate flowTemplate) {
        FlowTemplateEntity flowTemplateEntity = new FlowTemplateEntity();
        flowTemplateEntity.setId(flowTemplate.getId() == null ? IdGenerator.getId() : flowTemplate.getId());
        flowTemplateEntity.setName(flowTemplate.getName());
        if (flowTemplate.getNode() != null) {
            Node node = flowTemplate.getNode();
            if (node.getId() == null) {
                node.setId(IdGenerator.getId());
            }
        }
        flowTemplateEntity.setCode(flowTemplate.getCode());
        flowTemplateEntity.setStatus(flowTemplate.getStatus().getCode());
        return flowTemplateEntity;
    }

    public static void transferFormEntities(List<FormEntity> formEntities, Node node, Long tenantId) {
        if (node == null) {
            return;
        }
        FormEntity entity = toFormEntity(node.getForm(), tenantId, node.getId());
        formEntities.add(entity);
        if (CollectionUtils.isEmpty(node.getNext())) {
            return;
        }
        for (Node nextNode : node.getNext()) {
            transferFormEntities(formEntities, nextNode, tenantId);
        }
    }

    private static FormEntity toFormEntity(Form form, Long tenantId, Long nodeId) {
        FormEntity entity = new FormEntity();
        entity.setNodeId(nodeId);
        entity.setName(form.getName());
        entity.setCode(form.getCode());
        entity.setPath(form.getPath());
        entity.setTenantId(tenantId);
        return entity;
    }

    public static void transferFieldEntities(List<FieldEntity> fieldEntities, Node node, Long tenantId) {
        if (node == null) {
            return;
        }
        if (CollectionUtils.isEmpty(node.getForm().getFields())) {
            return;
        }
        for (Field field : node.getForm().getFields()) {
            FieldEntity entity = toFieldEntity(field, tenantId, node.getForm().getId());
            fieldEntities.add(entity);
        }
        if (CollectionUtils.isEmpty(node.getNext())) {
            return;
        }
        for (Node nextNode : node.getNext()) {
            transferFieldEntities(fieldEntities, nextNode, tenantId);
        }
    }

    private static FieldEntity toFieldEntity(Field field, Long tenantId, Long formId) {
        FieldEntity entity = new FieldEntity();
        entity.setId(field.getId() == null ? IdGenerator.getId() : field.getId());
        entity.setFormId(formId);
        entity.setTenantId(tenantId);
        entity.setName(field.getName());
        entity.setCode(field.getCode());
        return entity;
    }

    public static void transferPostEntity(List<PostEntity> postEntities, Node node, Long tenantId) {
        if (node == null) {
            return;
        }
        if (CollectionUtils.isEmpty(node.getBindPosts())) {
            return;
        }
        for (Post post : node.getBindPosts()) {
            postEntities.add(toPostEntity(post, tenantId, node.getId()));
        }
        if (CollectionUtils.isEmpty(node.getNext())) {
            return;
        }
        for (Node nextNode : node.getNext()) {
            transferPostEntity(postEntities, nextNode, tenantId);
        }
    }

    private static PostEntity toPostEntity(Post post, Long tenantId, Long nodeId) {
        PostEntity entity = new PostEntity();
        entity.setId(post.getPostId() == null ? IdGenerator.getId() : post.getId());
        entity.setPostId(post.getPostId());
        entity.setPostName(post.getPostName());
        entity.setNodeId(nodeId);
        entity.setTenantId(tenantId);
        return entity;
    }

    public static void transferNodeEntities(List<NodeEntity> nodeEntities, Node node, Long tenantId, Long parentNodeId, Long flowId, Long flowTemplateId) {
        if (node == null) {
            return;
        }
        NodeEntity nodeEntity = toNodeEntity(node, tenantId, parentNodeId, flowId, flowTemplateId);
        if (nodeEntity != null) {
            nodeEntities.add(nodeEntity);
        }
        if (CollectionUtils.isEmpty(node.getNext())) {
            return;
        }
        for (Node nextNode : node.getNext()) {
            transferNodeEntities(nodeEntities, nextNode, tenantId, nodeEntity.getId(), flowId, flowTemplateId);
        }
    }

    private static NodeEntity toNodeEntity(Node node, Long tenantId, Long parentId, Long flowId, Long flowTemplateId) {
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setId(node.getId() == null ? IdGenerator.getId() : node.getId());
        nodeEntity.setParentId(parentId);
        nodeEntity.setTenantId(tenantId);
        nodeEntity.setFlowId(flowId);
        nodeEntity.setFlowTemplateId(flowTemplateId);
        nodeEntity.setNodeType(node.getType().getCode());
        nodeEntity.setStatus(node.getStatus().getCode());
        nodeEntity.setProcessedBy(node.getProcessedBy());
        return nodeEntity;
    }

    public static FlowTemplate toFlowTemplate(FlowTemplateEntity flowTemplateEntity) {
        FlowTemplate flowTemplate = new FlowTemplate();
        flowTemplate.setId(flowTemplateEntity.getId());
        flowTemplate.setName(flowTemplateEntity.getName());
        flowTemplate.setCode(flowTemplateEntity.getCode());
        flowTemplate.setStatus(FlowTemplateStatusEnum.byCode(flowTemplateEntity.getStatus()));
        flowTemplate.setTenantId(flowTemplateEntity.getTenantId());
        return flowTemplate;
    }

    public static Node toNode(List<NodeEntity> nodeEntities) {
        if (CollectionUtils.isEmpty(nodeEntities)) {
            return null;
        }
        Node rootNode = findRootNode(nodeEntities);
        BusinessAssert.assertNotNull(rootNode, StatusCode.SERVICE_ERROR, "找不到根节点!");
        fillSubNodes(rootNode, nodeEntities);
        return rootNode;
    }

    private static Node findRootNode(List<NodeEntity> nodeEntities) {
        for (NodeEntity nodeEntity : nodeEntities) {
            if (nodeEntity.getParentId() == null || nodeEntity.getParentId() == 0L) {
                return toSingleNode(nodeEntity);
            }
        }
        return null;
    }

    private static Node toSingleNode(NodeEntity nodeEntity) {
        Node node = new Node();
        node.setId(nodeEntity.getId());
        node.setStatus(NodeStatusEnum.byCode(nodeEntity.getStatus()));
        node.setProcessedBy(node.getProcessedBy());
        return node;
    }

    private static void fillSubNodes(Node node, List<NodeEntity> nodeEntities) {
        List<NodeEntity> subNodes = nodeEntities.stream()
                .filter(nodeEntity -> nodeEntity.getParentId().equals(node.getId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(subNodes)) {
            return;
        }
        node.setNext(subNodes.stream().map(FlowEntityFactory::toSingleNode).collect(Collectors.toList()));

        for (Node subNode : node.getNext()) {
            fillSubNodes(subNode, nodeEntities);
        }
    }

    public static void fillNodeField(Node node, List<FieldEntity> fieldEntities) {
        if (node == null || node.getForm() == null) {
            return;
        }
        if (CollectionUtils.isEmpty(fieldEntities)) {
            return;
        }
        List<FieldEntity> matchedFieldEntities = fieldEntities.stream()
                .filter(fieldEntity -> fieldEntity.getFormId().equals(node.getForm().getId()))
                .collect(Collectors.toList());
        node.getForm().setFields(matchedFieldEntities.stream().map(FlowEntityFactory::toField).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(node.getNext())) {
            return;
        }
        for (Node subNode : node.getNext()) {
            fillNodeField(subNode, fieldEntities);
        }
    }

    public static Field toField(FieldEntity fieldEntity) {
        Field field = new Field();
        field.setId(fieldEntity.getId());
        field.setName(fieldEntity.getName());
        field.setCode(fieldEntity.getCode());
        return field;
    }

    public static void fillNodeForm(Node node, List<FormEntity> formEntities) {
        if (node == null) {
            return;
        }
        for (FormEntity formEntity : formEntities) {
            if (formEntity.getNodeId().equals(node.getId())) {
                node.setForm(toForm(formEntity));
                break;
            }
        }
        if (CollectionUtils.isEmpty(node.getNext())) {
            return;
        }
        for (Node subNode : node.getNext()) {
            fillNodeForm(subNode, formEntities);
        }
    }

    private static Form toForm(FormEntity formEntity) {
        Form form = new Form();
        form.setId(formEntity.getId());
        form.setName(formEntity.getName());
        form.setCode(formEntity.getCode());
        form.setPath(formEntity.getPath());
        return form;
    }

    public static void fillNodePost(Node node, List<PostEntity> postEntities) {
        if (node == null) {
            return;
        }
        List<PostEntity> bindPostEntities = postEntities.stream()
                .filter(postEntity -> postEntity.getNodeId().equals(node.getId()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(bindPostEntities)) {
            node.setBindPosts(bindPostEntities.stream().map(FlowEntityFactory::toPost).collect(Collectors.toList()));
        }
        if (CollectionUtils.isEmpty(node.getNext())) {
            return;
        }
        for (Node subNode : node.getNext()) {
            fillNodePost(subNode, postEntities);
        }
    }

    private static Post toPost(PostEntity postEntity) {
        Post post = new Post();
        post.setId(postEntity.getId());
        post.setPostId(postEntity.getPostId());
        post.setPostName(postEntity.getPostName());
        return post;
    }

    public static void getAllNodeId(List<Long> ids, Node node) {
        if (node == null) {
            return;
        }
        ids.add(node.getId());
        if (CollectionUtils.isEmpty(node.getNext())) {
            return;
        }
        getAllNodeId(ids, node);
    }

    public static void getAllFormId(List<Long> ids, Node node) {
        if (node == null || node.getForm() == null) {
            return;
        }
        ids.add(node.getForm().getId());
        if (CollectionUtils.isEmpty(node.getNext())) {
            return;
        }
        getAllFormId(ids, node);
    }
}
