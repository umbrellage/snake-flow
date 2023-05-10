package com.juliet.flow.service.impl;

import com.juliet.flow.client.dto.FieldDTO;
import com.juliet.flow.client.dto.FormDTO;
import com.juliet.flow.client.dto.PostDTO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.domain.dto.FlowTemplateAddDTO;
import com.juliet.flow.client.dto.NodeDTO;
import com.juliet.flow.domain.model.*;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.service.FlowTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Service
public class FlowTemplateServiceImpl implements FlowTemplateService {

    @Autowired
    private FlowRepository flowRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(FlowTemplateAddDTO flowTemplateAddDTO) {
        flowRepository.addTemplate(toFlowTemplate(flowTemplateAddDTO));
    }

    @Override
    public void update(FlowTemplateAddDTO flowTemplateAddDTO) {
        flowRepository.updateTemplate(toFlowTemplate(flowTemplateAddDTO));
    }

    @Override
    public void publish(Long flowTemplateId) {
        flowRepository.updateFlowTemplateStatusById(FlowTemplateStatusEnum.ENABLE, flowTemplateId);
    }

    @Override
    public void disable(Long flowTemplateId) {
        flowRepository.updateFlowTemplateStatusById(FlowTemplateStatusEnum.DISABLE, flowTemplateId);
    }

    private FlowTemplate toFlowTemplate(FlowTemplateAddDTO dto) {
        BusinessAssert.assertNotNull(dto.getNode(), StatusCode.ILLEGAL_PARAMS, "Node节点不能空!");
        Long tenantId = 1L;
        FlowTemplate flowTemplate = new FlowTemplate();
        Node node = new Node();
        flowTemplate.setId(dto.getId());
        toNode(node, dto.getNode());
        flowTemplate.setNode(node);
        flowTemplate.setName(dto.getName());
        flowTemplate.setStatus(FlowTemplateStatusEnum.IN_PROGRESS);
        flowTemplate.setTenantId(tenantId);
        return flowTemplate;
    }

    private void toNode(Node node, NodeDTO nodeDTO) {
        if (nodeDTO == null) {
            return;
        }
        node.setId(nodeDTO.getId() == null ? null : Long.valueOf(nodeDTO.getId()));
        node.setStatus(NodeStatusEnum.byCode(nodeDTO.getStatus()));
        node.setType(NodeTypeEnum.byCode(nodeDTO.getType()));
        node.setForm(toForm(nodeDTO.getForm()));
        if (!CollectionUtils.isEmpty(nodeDTO.getBindPosts())) {
            node.setBindPosts(nodeDTO.getBindPosts().stream()
                    .map(this::toPost).collect(Collectors.toList()));
        }
    }

    private Form toForm(FormDTO formDTO) {
        Form form = new Form();
        form.setId(formDTO.getId() == null ? null : Long.valueOf(formDTO.getId()));
        form.setName(formDTO.getName());
        form.setPath(formDTO.getPath());
        form.setCode(formDTO.getCode());
        if (!CollectionUtils.isEmpty(formDTO.getFields())) {
            form.setFields(formDTO.getFields().stream()
                    .map(this::toField)
                    .collect(Collectors.toList()));
        }
        return form;
    }

    private Field toField(FieldDTO fieldDTO) {
        Field field = new Field();
        field.setId(fieldDTO.getId() == null ? null : Long.valueOf(fieldDTO.getId()));
        field.setName(fieldDTO.getName());
        field.setCode(fieldDTO.getCode());
        return field;
    }

    private Post toPost(PostDTO postDTO) {
        Post post = new Post();
        post.setId(postDTO.getId() == null ? null : Long.valueOf(postDTO.getId()));
        post.setPostId(postDTO.getPostId());
        post.setPostName(postDTO.getPostName());
        return post;
    }
}
