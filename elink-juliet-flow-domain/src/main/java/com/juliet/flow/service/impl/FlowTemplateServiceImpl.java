package com.juliet.flow.service.impl;

import com.juliet.api.development.domain.entity.SysUser;
import com.juliet.common.security.utils.SecurityUtils;
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
import com.juliet.flow.domain.model.rule.RuleFactory;
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
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        flowTemplateAddDTO.setCreateBy(sysUser.getUserId());
        flowTemplateAddDTO.setUpdateBy(sysUser.getUserId());
        flowTemplateAddDTO.setTenantId(sysUser.getTenantId());
        flowRepository.addTemplate(toFlowTemplate(flowTemplateAddDTO));
    }

    @Override
    public void update(FlowTemplateAddDTO flowTemplateAddDTO) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        flowTemplateAddDTO.setUpdateBy(sysUser.getUserId());
        flowTemplateAddDTO.setTenantId(sysUser.getTenantId());
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
        BusinessAssert.assertNotEmpty(dto.getNodes(), StatusCode.ILLEGAL_PARAMS, "Node节点不能空!");
        FlowTemplate flowTemplate = new FlowTemplate();
        flowTemplate.setId(dto.getId());
        flowTemplate.setNodes(dto.getNodes().stream()
                .map(nodeDTO -> toNode(nodeDTO, dto.getCreateBy(), dto.getUpdateBy()))
                .collect(Collectors.toList()));
        flowTemplate.setName(dto.getName());
        flowTemplate.setCode(dto.getCode());
        flowTemplate.setStatus(FlowTemplateStatusEnum.IN_PROGRESS);
        flowTemplate.setTenantId(dto.getTenantId());
        flowTemplate.setCreateBy(dto.getCreateBy());
        flowTemplate.setUpdateBy(dto.getUpdateBy());
        return flowTemplate;
    }

    private Node toNode(NodeDTO nodeDTO, Long createBy, Long updateBy) {
        if (nodeDTO == null) {
            return null;
        }
        Node node = new Node();
        node.setId(nodeDTO.getId() == null ? null : Long.valueOf(nodeDTO.getId()));
        node.setTitle(nodeDTO.getTitle());
        node.setName(nodeDTO.getName());
        node.setPreName(nodeDTO.getPreName());
        node.setNextName(nodeDTO.getNextName());
        node.setStatus(NodeStatusEnum.byCode(nodeDTO.getStatus()));
        node.setType(NodeTypeEnum.byCode(nodeDTO.getType()));
        node.setSupervisorIds(nodeDTO.getSupervisorIds());
        node.setRuleAssignment(nodeDTO.getRuleAssignment());
        node.setSupervisorAssignment(nodeDTO.getSupervisorAssignment());
        node.setSelfAndSupervisorAssignment(nodeDTO.getSelfAndSupervisorAssignment());
        node.setAssignRule(RuleFactory.getAssignRule(nodeDTO.getAssignRuleName()));
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
        return node;
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
