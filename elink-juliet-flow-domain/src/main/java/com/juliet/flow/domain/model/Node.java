package com.juliet.flow.domain.model;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.common.NotifyTypeEnum;
import com.juliet.flow.client.dto.AccessRuleDTO;
import com.juliet.flow.client.dto.AssignmentRuleDTO;
import com.juliet.flow.client.dto.NotifyDTO;
import com.juliet.flow.client.dto.SupplierDTO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.client.vo.PostVO;
import com.juliet.flow.client.vo.ProcessedByVO;
import com.juliet.flow.client.vo.SupplierVO;
import com.juliet.flow.client.common.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import com.juliet.flow.client.common.TodoNotifyEnum;
import com.juliet.flow.common.utils.IdGenerator;
import com.juliet.flow.domain.entity.NodeEntity;

import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Data
public class Node extends BaseModel {

    private Long id;

    private String externalNodeId;

    private Long flowId;

    private Long mainFlowId;

    private Long titleId;

    private String title;

    private String name;

    private String preName;

    private String nextName;

    /**
     * 表单
     */
    private Form form;

    private NodeStatusEnum status;

    private String customStatus;

    private NodeTypeEnum type;

    private List<Post> bindPosts = new ArrayList<>();

    private List<Supplier> bindSuppliers;
    /**
     * 准入规则
     */
    private BaseRule accessRule;

    /**
     * 提交规则
     */
    private BaseRule submitRule;

    private NotifyRule activeRule;

    private FlowAutomateRule flowAutomateRule;

    private String flowAutomateRuleName;

    /**
     * 主管分配
     */
    private Boolean supervisorAssignment;

    /**
     * 认领+调整
     */
    private Boolean selfAndSupervisorAssignment;

    /**
     * 规则分配
     */
    private Boolean ruleAssignment;

    /**
     * 分配规则, 分配操作人
     */
    private BaseAssignRule assignRule;

    /**
     * 主管ID列表
     */
    private List<Long> supervisorIds;

    @ApiModelProperty("是否分配给流程内的节点操作人")
    private Boolean flowInnerAssignment;

    @ApiModelProperty("从分配的节点里获取操作人")
    private String distributeNode;

    /**
     * 处理人
     */
    private Long processedBy;
    private LocalDateTime processedTime;
    /**
     * 是否发送消息通知
     */
    private TodoNotifyEnum todoNotify;
    /**
     * 修改其他节点待办配置
     */
    private String modifyOtherTodoName;

    private List<AssignmentRuleDTO> ruleList;

    private List<AccessRuleDTO> accessRuleList;

    private List<AccessRuleDTO> forwardRuleList;

    private List<AccessRuleDTO> rollbackRuleList;


    public boolean ifLeaderAdjust(Long userId) {
        // userId为null或者为0说明不是主管调整处理人，放行不需要校验
        if (userId == null || userId == 0L) {
            return true;
        }
        return supervisorIds.contains(userId);
    }

    public List<Long> getSupervisorIds() {
        return supervisorIds;
    }


    public String supervisorIds() {
        if (CollectionUtils.isEmpty(supervisorIds)) {
            return "";
        }
        return supervisorIds.stream().map(this::formatOf).collect(Collectors.joining(","));
    }

    /**
     * supervisorId 格式修改，如需修改前后缀字符，请一起修改以下方法
     *
     * @param supervisorId
     * @return
     * @see NodeEntity#supervisorIds()
     */
    public String formatOf(Long supervisorId) {
        if (supervisorId == null) {
            return null;
        }
        return "^" + supervisorId + "^";
    }

    public LocalDateTime processedTime() {
        if (processedBy == null || processedBy == 0L) {
            return null;
        }
        return processedTime;
    }

    public void regularDistribution(Map<String, Object> params, Flow flow) {
        if (Boolean.TRUE.equals(ruleAssignment) && assignRule != null) {
            Long assignProcessedBy = assignRule.getAssignUserId(params, flow, id);
            if (assignProcessedBy != null) {
                processedBy = assignProcessedBy;
            }
            SupplierDTO supplierDTO = assignRule.getAssignSupplier(params);
            if (supplierDTO != null && supplierDTO.getSupplierId() != null) {
                // todo 清空post,因为现在没有做供应商的岗位配置所以在供应商分配清楚岗位不然品牌方会收到待办
                bindPosts = Collections.emptyList();
                Supplier supplier = new Supplier();
                supplier.setSupplierId(Long.valueOf(supplierDTO.getSupplierId()));
                supplier.setSupplierType(supplierDTO.getSupplierType());
                supplier.setSupplierName(supplierDTO.getSupplierName());
                bindSuppliers = Collections.singletonList(supplier);
            }
        }
    }

    public void regularFlowInnerOperator(Flow flow) {
        if (StringUtils.isBlank(distributeNode)) {
            return;
        }
        if (flowInnerAssignment == null || !flowInnerAssignment) {
            return;
        }
        flow.getNodes().stream()
            .filter(node -> StringUtils.equals(node.getExternalNodeId(), distributeNode) ||
                StringUtils.equals(node.getName(), distributeNode))
            .findAny()
            .ifPresent(node -> {
                this.processedBy = node.getProcessedBy();
                this.processedTime = LocalDateTime.now();
            });
    }

    public NotifyDTO toNotifyNormal(Flow flow) {
        NotifyDTO ret = new NotifyDTO();
        ret.setNodeId(id);
        ret.setNodeName(name);
        ret.setNodeVO(toNodeVo(flow));
        ret.setFlowId(flowId == null ? flow.getId(): flowId);
        ret.setTodoNotify(todoNotify);
        if (form != null && CollectionUtils.isNotEmpty(form.getFields())) {
            ret.setFiledList(form.getFields().stream().map(Field::getCode).collect(Collectors.toList()));
        }

        ret.setSupervisorIds(supervisorIds);
        ret.setCode(flow.getTemplateCode());
        ret.setPostIdList(postIdList());
        ret.setUserId(processedBy);
        ret.setMainFlowId(flow.getParentId());
        if (supervisorAssignment) {
            ret.setType(NotifyTypeEnum.SUPERVISOR_ASSIGNMENT);
        }
        if (selfAndSupervisorAssignment) {
            ret.setType(NotifyTypeEnum.SELF_AND_SUPERVISOR_ASSIGNMENT);
        }
        ret.setTenantId(getTenantId());
        ret.setPreprocessedBy(processedByList(flow));
        return ret;
    }

    public NotifyDTO toNotifyComplete(Flow flow) {
        NotifyDTO ret = new NotifyDTO();
        ret.setNodeId(id);
        ret.setTodoNotify(todoNotify);
        ret.setNodeName(name);
        ret.setNodeVO(toNodeVo(flow));
        ret.setFlowId(flowId);
        ret.setMainFlowId(flow.getParentId());
        ret.setType(NotifyTypeEnum.COMPLETE);
        ret.setCode(flow.getTemplateCode());
        ret.setTenantId(getTenantId());
        return ret;
    }

    public NotifyDTO toNotifyDelete(Flow flow) {
        NotifyDTO ret = new NotifyDTO();
        ret.setNodeId(id);
        ret.setTodoNotify(todoNotify);
        ret.setNodeName(name);
        ret.setNodeVO(toNodeVo(flow));
        ret.setFlowId(flowId);
        ret.setMainFlowId(flow.getParentId());
        ret.setType(NotifyTypeEnum.DELETE);
        ret.setCode(flow.getTemplateCode());
        ret.setTenantId(getTenantId());

        return ret;
    }

    public NotifyDTO toNotifyCC(Flow flow, String remark) {
        NotifyDTO ret = new NotifyDTO();
        ret.setNodeId(id);
        ret.setNodeName(name);
        ret.setNodeVO(toNodeVo(flow));
        ret.setTodoNotify(todoNotify);
        ret.setFlowId(flowId);
        ret.setUserId(processedBy);
        ret.setMainFlowId(flow.getParentId());
        ret.setType(NotifyTypeEnum.CC);
        ret.setTenantId(getTenantId());
        ret.setRemark(remark);
        ret.setCode(flow.getTemplateCode());
        return ret;
    }


    public List<String> postIdList() {
        if (CollectionUtils.isEmpty(bindPosts)) {
            return Collections.emptyList();
        }
        return bindPosts.stream()
                .map(Post::getPostId)
                .collect(Collectors.toList());
    }

    /**
     * 判断该岗位是否有该节点权限
     *
     * @param postIdList
     * @return
     */
    public boolean postAuthority(List<Long> postIdList) {
        if (CollectionUtils.isEmpty(bindPosts)) {
            throw new ServiceException("当前节点没有绑定权限");
        }
        // 如果存在-1,则任何人都可以发起
        if (bindPosts.stream().anyMatch(bindPost -> "-1".equals(bindPost.getPostId()))) {
            return true;
        }
        List<Long> sourcePostIdList = bindPosts.stream()
                .map(Post::getPostId)
                .filter(Objects::nonNull)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        return !Collections.disjoint(postIdList, sourcePostIdList);
    }

    /**
     * 判断节点是否已经存在处理人
     *
     * @return
     */
    public boolean nodeTodo() {
        return processedBy != null && processedBy != 0;
    }


    /**
     * 获取前置节点列表
     *
     * @return
     */
    public List<String> preNameList() {
        if (StringUtils.isBlank(preName)) {
            return Collections.emptyList();
        }
        return Arrays.stream(preName.split(","))
                .collect(Collectors.toList());
    }

    /**
     * 获取后置节点列表
     *
     * @return
     */
    public List<String> nextNameList() {
        if (StringUtils.isBlank(nextName)) {
            return Collections.emptyList();
        }
        return Arrays.stream(nextName.split(","))
                .collect(Collectors.toList());
    }

    /**
     * 节点是否已处理
     *
     * @return
     */
    public boolean isProcessed() {
        return status == NodeStatusEnum.PROCESSED;
    }

    /**
     * 判断节点是否是一个可被执行的
     *
     * @return
     */
    public boolean isExecutable() {
        return status == NodeStatusEnum.PROCESSED || status == NodeStatusEnum.ACTIVE || todoNotify == TodoNotifyEnum.NO_NOTIFY;
    }

    /**
     * 判断节点是否是一个可被正常执行的
     *
     * @return
     */
    public boolean isNormalExecutable() {
        return status == NodeStatusEnum.ACTIVE;
    }

    /**
     * 判断节点是否已认领，待被执行
     *
     * @return
     */
    public boolean isToBeExecuted() {
        return status == NodeStatusEnum.ACTIVE;
    }

    /**
     * 判断节点是否是待办的
     *
     * @return
     */
    public boolean isTodoNode() {
        return (status == NodeStatusEnum.ACTIVE || status == NodeStatusEnum.TO_BE_CLAIMED) && todoNotify == TodoNotifyEnum.NOTIFY;
    }

    /**
     * 判断节点是否是待办的
     *
     * @return
     */
    public boolean needCallbackMsg() {
        return status == NodeStatusEnum.ACTIVE || status == NodeStatusEnum.TO_BE_CLAIMED;
    }

    /**
     *
     * @return
     */
    public boolean nodeCanEdit() {
        return status == NodeStatusEnum.ACTIVE || status == NodeStatusEnum.TO_BE_CLAIMED;
    }



    /**
     * 通过岗位判断当前用户是否可以操作
     */
    public boolean isOperator(Long[] postIds) {
        if (ArrayUtils.isEmpty(postIds)) {
            return false;
        }
        if (CollectionUtils.isEmpty(bindPosts)) {
            return false;
        }
        for (Long postId : postIds) {
            if (bindPosts.stream().anyMatch(post -> post.getPostId().equals(String.valueOf(postId)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否某个用户可办
     */
    public boolean isUserCando(Long userId, List<Long> postIds, Long supplierId) {
        if (todoNotify != TodoNotifyEnum.NO_NOTIFY) {
            return false;
        }
        if (status == NodeStatusEnum.ACTIVE) {
            return userId != null && userId.equals(processedBy);
        }
        if (status == NodeStatusEnum.TO_BE_CLAIMED) {
            return isPostMatch(postIds) || isSupplierMatch(supplierId);
        }
        return false;
    }

    public boolean isSupplierMatch(Long supplierId) {

        if (CollectionUtils.isEmpty(bindSuppliers)) {
            return false;
        }
        return bindSuppliers.stream().anyMatch(e -> Objects.equals(e.getSupplierId(), supplierId));

    }

    /**
     * 判断岗位是否匹配
     */
    public boolean isPostMatch(List<Long> postIds) {
        if (CollectionUtils.isEmpty(bindPosts)) {
            return false;
        }
        if (bindPosts.stream().anyMatch(post -> "-1".equals(post.getPostId()))) {
            return true;
        }
        return !Collections.disjoint(postIds.stream().map(String::valueOf).collect(Collectors.toList()), bindPosts.stream()
                .map(Post::getPostId)
                .collect(Collectors.toList()));
    }

    /**
     * @param flow 当前流程
     * @return
     */
    public NodeVO toNodeVo(Flow flow) {
        NodeVO data = new NodeVO();
        data.setId(id);
        data.setName(name);
        data.setExternalNodeId(externalNodeId);
        data.setTitleId(titleId == null ? null : String.valueOf(titleId));
        data.setTitle(title);
        data.setFlowId(flowId);
        data.setPreName(preName);
        data.setNextName(nextName);
        data.setSelfAndSupervisorAssignment(selfAndSupervisorAssignment);
        data.setFlowInnerAssignment(flowInnerAssignment);
        data.setDistributeNode(distributeNode);
        data.setSupervisorAssignment(supervisorAssignment);
        if (status != null) {
            data.setStatus(status.getCode());
        }
        data.setCustomStatus(customStatus);
        Optional.ofNullable(form).ifPresent(form -> data.setForm(form.toForm()));
        data.setProcessedBy(processedBy);
        if (CollectionUtils.isNotEmpty(bindPosts)) {
            List<PostVO> postVOList = bindPosts.stream()
                    .map(Post::toPost)
                    .collect(Collectors.toList());
            data.setBindPosts(postVOList);
        }
        if (CollectionUtils.isNotEmpty(bindSuppliers)) {
            List<SupplierVO> supplierVOList = bindSuppliers.stream()
                .map(e -> {
                    SupplierVO supplierVO = new SupplierVO();
                    BeanUtils.copyProperties(e, supplierVO);
                    return supplierVO;
                })
                .collect(Collectors.toList());
            data.setBindSuppliers(supplierVOList);
        }
        data.setSupervisorIds(supervisorIds);
        data.setProcessedTime(processedTime);

        if (flow != null) {
            data.setCode(flow.getTemplateCode());
            data.setMainFlowId(flow.getParentId() == null || flow.getParentId() == 0 ? flow.getId() : flow.getParentId());
            List<ProcessedByVO> preProcessedBy = processedByList(flow);
            data.setPreprocessedBy(preProcessedBy);
        }
        data.setTenantId(getTenantId());
        return data;
    }

    public List<ProcessedByVO> processedByList(Flow flow) {
        return preNameList().stream()
                .map(flow::findNode)
                .filter(Objects::nonNull)
                .map(node -> ProcessedByVO.of(node.getId(), node.getProcessedBy(), node.processedTime()))
                .collect(Collectors.toList());
    }

    public Node copyNode() {
        Node node = new Node();
        node.id = IdGenerator.getId();
        node.titleId = titleId;
        node.title = title;
        node.customStatus = customStatus;
        node.externalNodeId = externalNodeId;
        node.name = name;
        node.preName = preName;
        node.nextName = nextName;
        if (form != null) {
            Form newForm = new Form();
            newForm.setId(IdGenerator.getId());
            newForm.setName(form.getName());
            newForm.setPath(form.getPath());
            newForm.setCode(form.getCode());
            newForm.setFields(form.getFields().stream().map(Field::deepCopy).collect(Collectors.toList()));
            newForm.setCreateBy(form.getCreateBy());
            newForm.setCreateTime(new Date());
            newForm.setUpdateBy(form.getUpdateBy());
            newForm.setUpdateTime(new Date());
            newForm.setTenantId(form.getTenantId());
            node.form = newForm;
        }
        node.status = status;
        node.type = type;
        if (CollectionUtils.isNotEmpty(bindPosts)) {
            node.bindPosts = bindPosts.stream()
                .map(Post::deepCopy)
                .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(bindSuppliers)) {
            node.bindSuppliers = bindSuppliers.stream()
                .map(Supplier::deepCopy)
                .collect(Collectors.toList());
        }
        node.accessRule = accessRule;
        node.submitRule = submitRule;
        node.processedBy = processedBy;
        node.supervisorIds = supervisorIds;
        node.assignRule = assignRule;
        node.ruleAssignment = ruleAssignment;
        node.distributeNode = distributeNode;
        node.flowInnerAssignment = flowInnerAssignment;
        node.selfAndSupervisorAssignment = selfAndSupervisorAssignment;
        node.supervisorAssignment = supervisorAssignment;
        node.setCreateBy(this.getCreateBy());
        node.setUpdateBy(0L);
        node.setCreateTime(new Date());
        node.setUpdateTime(getUpdateTime());
        node.setTenantId(this.getTenantId());
        node.setTodoNotify(todoNotify);
        node.setProcessedTime(processedTime);
        node.modifyOtherTodoName = modifyOtherTodoName;
        node.accessRuleList = accessRuleList;
        node.flowAutomateRuleName = flowAutomateRuleName;
        node.forwardRuleList = forwardRuleList;
        node.rollbackRuleList = rollbackRuleList;
        node.ruleList = ruleList;
        return node;
    }


}
