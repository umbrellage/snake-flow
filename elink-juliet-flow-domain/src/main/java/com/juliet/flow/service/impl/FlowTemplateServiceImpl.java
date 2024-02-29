package com.juliet.flow.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson2.JSON;
import com.juliet.api.development.domain.entity.SysUser;
import com.juliet.common.core.exception.ServiceException;
import com.juliet.common.security.utils.SecurityUtils;
import com.juliet.flow.client.dto.*;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import com.juliet.flow.client.common.TodoNotifyEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.client.dto.FlowTemplateAddDTO;
import com.juliet.flow.domain.model.*;
import com.juliet.flow.domain.model.rule.RuleFactory;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.service.FlowTemplateService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Slf4j
@Service
public class FlowTemplateServiceImpl implements FlowTemplateService {

    @Autowired
    private FlowRepository flowRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long add(FlowTemplateAddDTO flowTemplateAddDTO) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        flowTemplateAddDTO.setCreateBy(sysUser.getUserId());
        flowTemplateAddDTO.setUpdateBy(sysUser.getUserId());
        flowTemplateAddDTO.setTenantId(sysUser.getTenantId());
        return flowRepository.addTemplate(toFlowTemplate(flowTemplateAddDTO));
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
    public String updateTimeByCode(String code) {
        FlowTemplate flowTemplate = flowRepository.queryTemplateByCode(code);
        if (flowTemplate.getUpdateTime() == null) {
            return "0";
        }
        return String.valueOf(flowTemplate.getUpdateTime().getTime());
    }

    @Override
    public void publish(Long flowTemplateId) {
        flowRepository.updateFlowTemplateStatusById(FlowTemplateStatusEnum.ENABLE, flowTemplateId);
    }

    @Override
    public void disable(Long flowTemplateId) {
        flowRepository.updateFlowTemplateStatusById(FlowTemplateStatusEnum.DISABLE, flowTemplateId);
    }

    @Override
    public List<NodeVO> nodeList(Long id) {
        FlowTemplate flowTemplate = flowRepository.queryTemplateById(id);
        Optional.ofNullable(flowTemplate).orElseThrow(() -> new ServiceException("流程模版不存在"));
        return flowTemplate.getNodes()
            .stream()
            .map(e -> e.toNodeVo(null))
            .collect(Collectors.toList());
    }

    private FlowTemplate toFlowTemplate(FlowTemplateAddDTO dto) {
        BusinessAssert.assertNotEmpty(dto.getNodes(), StatusCode.ILLEGAL_PARAMS, "Node节点不能空!");
        FlowTemplate flowTemplate = new FlowTemplate();
        flowTemplate.setId(dto.getId());
        flowTemplate.setNodes(dto.getNodes().stream()
                .map(nodeDTO -> toNode(nodeDTO, dto.getCreateBy(), dto.getUpdateBy(), dto.getTenantId()))
                .collect(Collectors.toList()));
        flowTemplate.setName(dto.getName());
        flowTemplate.setCode(dto.getCode());
        flowTemplate.setDto(dto.getDto());
        flowTemplate.setStatus(FlowTemplateStatusEnum.IN_PROGRESS);
        flowTemplate.setTenantId(dto.getTenantId());
        flowTemplate.setCreateBy(dto.getCreateBy());
        flowTemplate.setUpdateBy(dto.getUpdateBy());
        return flowTemplate;
    }

    private Node toNode(NodeDTO nodeDTO, Long createBy, Long updateBy, Long flowTenantId) {
        if (nodeDTO == null) {
            return null;
        }
        log.info("nodeDTO:{}", JSON.toJSONString(nodeDTO));
        Node node = new Node();
        node.setId(nodeDTO.getId() == null ? null : Long.valueOf(nodeDTO.getId()));
        node.setTitle(nodeDTO.getTitle());
        node.setName(nodeDTO.getName());
        node.setRuleList(nodeDTO.getAssignmentRuleList());
        node.setExternalNodeId(nodeDTO.getExternalNodeId());
        node.setPreName(nodeDTO.getPreName());
        node.setNextName(nodeDTO.getNextName());
        node.setStatus(NodeStatusEnum.byCode(nodeDTO.getStatus()));
        node.setCustomStatus(nodeDTO.getCustomStatus());
        node.setType(NodeTypeEnum.byCode(nodeDTO.getType()));
        node.setAccessRule(RuleFactory.getAccessRule(nodeDTO.getAccessRuleName()));
        node.setSupervisorIds(nodeDTO.getSupervisorIds());
        node.setRuleAssignment(nodeDTO.getRuleAssignment());
        node.setSupervisorAssignment(nodeDTO.getSupervisorAssignment());
        node.setSelfAndSupervisorAssignment(nodeDTO.getSelfAndSupervisorAssignment());
        node.setAssignRule(RuleFactory.getAssignRule(nodeDTO.getAssignRuleName()));
        
        node.setTenantId(StringUtil.isBlank(nodeDTO.getTenantId()) ? flowTenantId : Long.valueOf(nodeDTO.getTenantId()));
        if (nodeDTO.getTodoNotify() == null) {
            nodeDTO.setTodoNotify(TodoNotifyEnum.NOTIFY.getCode());
        }
        node.setTodoNotify(TodoNotifyEnum.of(nodeDTO.getTodoNotify()));
        node.setModifyOtherTodoName(nodeDTO.getModifyOtherTodoName() == null ? "" : nodeDTO.getModifyOtherTodoName());
        node.setCreateBy(createBy);
        node.setUpdateBy(updateBy);
        if (nodeDTO.getForm() != null) {
            node.setForm(toForm(nodeDTO.getForm(), createBy, updateBy, node.getTenantId()));
        }
        if (!CollectionUtils.isEmpty(nodeDTO.getBindPosts())) {
            node.setBindPosts(nodeDTO.getBindPosts().stream()
                    .map(postDTO -> toPost(postDTO, createBy, updateBy, node.getTenantId()))
                    .collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(nodeDTO.getBindSuppliers())) {
            node.setBindSuppliers(nodeDTO.getBindSuppliers().stream()
                    .map(supplierDTO -> toSupplier(supplierDTO, createBy, updateBy, node.getTenantId()))
                    .collect(Collectors.toList()));
        }
        return node;
    }

    private Form toForm(FormDTO formDTO, Long createBy, Long updateBy, Long nodeTenantId) {
        Form form = new Form();
        form.setId(formDTO.getId() == null ? null : Long.valueOf(formDTO.getId()));
        form.setName(formDTO.getName());
        form.setPath(formDTO.getPath());
        form.setCode(formDTO.getCode());
        form.setCreateBy(createBy);
        form.setUpdateBy(updateBy);
        form.setTenantId(nodeTenantId);
        if (!CollectionUtils.isEmpty(formDTO.getFields())) {
            form.setFields(formDTO.getFields().stream()
                    .map(fieldDTO -> toField(fieldDTO, createBy, updateBy, nodeTenantId))
                    .collect(Collectors.toList()));
        }
        return form;
    }

    private Field toField(FieldDTO fieldDTO, Long createBy, Long updateBy, Long nodeTenantId) {
        Field field = new Field();
        field.setId(fieldDTO.getId() == null ? null : Long.valueOf(fieldDTO.getId()));
        field.setName(fieldDTO.getName());
        field.setCode(fieldDTO.getCode());
        field.setCreateBy(createBy);
        field.setUpdateBy(updateBy);
        field.setTenantId(nodeTenantId);
        return field;
    }

    private Post toPost(PostDTO postDTO, Long createBy, Long updateBy, Long nodeTenantId) {
        Post post = new Post();
        post.setId(postDTO.getId() == null ? null : Long.valueOf(postDTO.getId()));
        post.setPostId(postDTO.getPostId());
        post.setPostName(postDTO.getPostName());
        post.setCreateBy(createBy);
        post.setUpdateBy(updateBy);
        post.setTenantId(nodeTenantId);
        return post;
    }

    private Supplier toSupplier(SupplierDTO supplierDTO, Long createBy, Long updateBy, Long nodeTenantId) {
        Supplier supplier = new Supplier();
        supplier.setId(supplierDTO.getId() == null ? null : Long.valueOf(supplierDTO.getId()));
        supplier.setSupplierType(supplierDTO.getSupplierType());
        supplier.setSupplierId(Long.valueOf(supplierDTO.getSupplierId()));
        supplier.setCreateBy(createBy);
        supplier.setUpdateBy(updateBy);
        supplier.setTenantId(nodeTenantId);
        return supplier;
    }

}
