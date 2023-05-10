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

import java.util.ArrayList;
import java.util.List;
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
        flowTemplateAddDTO.setCreateBy(1L);
        flowTemplateAddDTO.setUpdateBy(1L);
        flowRepository.addTemplate(toFlowTemplate(flowTemplateAddDTO));
    }

    @Override
    public void update(FlowTemplateAddDTO flowTemplateAddDTO) {
        flowTemplateAddDTO.setUpdateBy(2L);
        flowRepository.updateTemplate(toFlowTemplate(flowTemplateAddDTO));
    }

    @Override
    public FlowTemplate queryById(Long id) {
        return flowRepository.queryTemplateById(id);
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
        toNode(node, dto.getNode(), dto.getCreateBy(), dto.getUpdateBy());
//        flowTemplate.setNode(node);
        flowTemplate.setName(dto.getName());
        flowTemplate.setCode(dto.getCode());
        flowTemplate.setStatus(FlowTemplateStatusEnum.IN_PROGRESS);
        flowTemplate.setTenantId(tenantId);
        flowTemplate.setCreateBy(dto.getCreateBy());
        flowTemplate.setUpdateBy(dto.getUpdateBy());
        return flowTemplate;
    }

    private void toNode(Node node, NodeDTO nodeDTO, Long createBy, Long updateBy) {
        if (nodeDTO == null) {
            return;
        }
        node.setId(nodeDTO.getId() == null ? null : Long.valueOf(nodeDTO.getId()));
        node.setStatus(NodeStatusEnum.byCode(nodeDTO.getStatus()));
        node.setType(NodeTypeEnum.byCode(nodeDTO.getType()));
        node.setCreateBy(createBy);
        node.setUpdateBy(updateBy);
        if (nodeDTO.getForm() != null) {
            node.setForm(toForm(nodeDTO.getForm(), createBy, updateBy));
        }
        if (!CollectionUtils.isEmpty(nodeDTO.getBindPosts())) {
            node.setBindPosts(nodeDTO.getBindPosts().stream()
                    .map(postDTO -> toPost(postDTO, createBy, updateBy))
                    .collect(Collectors.toList()));
        }
        if (CollectionUtils.isEmpty(nodeDTO.getNext())) {
            return;
        }
        for (NodeDTO subNodeDTO : nodeDTO.getNext()) {
            Node subNode = new Node();
            List<Node> list = node.getNext();
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(subNode);
            node.setNext(list);
            toNode(subNode, subNodeDTO, createBy, updateBy);
        }
    }

    private Form toForm(FormDTO formDTO, Long createBy, Long updateBy) {
        Form form = new Form();
        form.setId(formDTO.getId() == null ? null : Long.valueOf(formDTO.getId()));
        form.setName(formDTO.getName());
        form.setPath(formDTO.getPath());
        form.setCode(formDTO.getCode());
        form.setCreateBy(createBy);
        form.setUpdateBy(updateBy);
        if (!CollectionUtils.isEmpty(formDTO.getFields())) {
            form.setFields(formDTO.getFields().stream()
                    .map(fieldDTO -> toField(fieldDTO, createBy, updateBy))
                    .collect(Collectors.toList()));
        }
        return form;
    }

    private Field toField(FieldDTO fieldDTO, Long createBy, Long updateBy) {
        Field field = new Field();
        field.setId(fieldDTO.getId() == null ? null : Long.valueOf(fieldDTO.getId()));
        field.setName(fieldDTO.getName());
        field.setCode(fieldDTO.getCode());
        field.setCreateBy(createBy);
        field.setUpdateBy(updateBy);
        return field;
    }

    private Post toPost(PostDTO postDTO, Long createBy, Long updateBy) {
        Post post = new Post();
        post.setId(postDTO.getId() == null ? null : Long.valueOf(postDTO.getId()));
        post.setPostId(postDTO.getPostId());
        post.setPostName(postDTO.getPostName());
        post.setCreateBy(createBy);
        post.setUpdateBy(updateBy);
        return post;
    }
}
