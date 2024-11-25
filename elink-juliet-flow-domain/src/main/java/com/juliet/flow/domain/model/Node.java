package com.juliet.flow.domain.model;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.common.core.utils.SpringUtils;
import com.juliet.flow.client.common.NodeStatusEnum;
import com.juliet.flow.client.common.NotifyTypeEnum;
import com.juliet.flow.client.common.OperateTypeEnum;
import com.juliet.flow.client.common.TodoNotifyEnum;
import com.juliet.flow.client.dto.AccessRuleDTO;
import com.juliet.flow.client.dto.AssignmentRuleDTO;
import com.juliet.flow.client.dto.NotifyDTO;
import com.juliet.flow.client.dto.SupplierDTO;
import com.juliet.flow.client.vo.GraphEdgeVO;
import com.juliet.flow.client.vo.GraphNodeVO;
import com.juliet.flow.client.vo.GraphVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.client.vo.PostVO;
import com.juliet.flow.client.vo.ProcessedByVO;
import com.juliet.flow.client.vo.SupplierVO;
import com.juliet.flow.common.enums.NodeTypeEnum;
import com.juliet.flow.common.utils.IdGenerator;
import com.juliet.flow.domain.entity.NodeEntity;
import com.juliet.flow.repository.FlowRepository;
import io.swagger.annotations.ApiModelProperty;
import java.time.Duration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Slf4j
@Data
public class Node extends BaseModel {

    private Long id;

    private String externalNodeId;

    private Long flowId;

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

    // 自动分配不为空时，是系统节点
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
     * 岗位下可以认领的人
     */
    private List<Long> claimableUserIds;

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

    /**
     * 节点激活时间
     */
    private LocalDateTime activeTime;
    /**
     * 节点认领时间
     */
    private LocalDateTime claimTime;

    private LocalDateTime finishTime;


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

    public String claimableUserIds() {
        if (CollectionUtils.isEmpty(claimableUserIds)) {
            return "";
        }
        return claimableUserIds.stream().map(this::formatOf).collect(Collectors.joining(","));
    }

    /**
     * id 格式修改，如需修改前后缀字符，请一起修改以下方法
     *
     * @param id
     * @return
     * @see NodeEntity#supervisorIds()
     */
    public String formatOf(Long id) {
        if (id == null) {
            return null;
        }
        return "^" + id + "^";
    }


    public LocalDateTime processedTime() {
        // 系统节点
        // 判断系统节点：StringUtils.isNotBlank(flowAutomateRuleName)
        // 如果节点是完成的，则返回
        if (status == NodeStatusEnum.PROCESSED) {
            return processedTime;
        }
        if (processedBy == null || processedBy == 0L) {
            return null;
        }
        return processedTime;

    }

    public LocalDateTime processedTimeV2() {
        if (finishTime != null) {
            return finishTime;
        }
        if (activeTime != null) {
            return activeTime;
        }
        return claimTime;
    }

    /**
     * 是否存在操作人
     * @return true/false
     */
    public boolean existOperator() {
        return processedBy != null && processedBy != 0 && StringUtils.isBlank(flowAutomateRuleName);
    }

    public void regularDistribution(Map<String, Object> params, Flow flow) {
        if (Boolean.TRUE.equals(ruleAssignment) && assignRule != null) {
            Long assignProcessedBy = assignRule.getAssignUserId(params, flow, id);
            if (assignProcessedBy != null) {
                log.info("setNodeUserId regularDistribution nodeId:{}, setProcessedBy:{}", id, assignProcessedBy);
                processedBy = assignProcessedBy;
                processedTime = LocalDateTime.now();
                claimTime = LocalDateTime.now();
            }
            SupplierDTO supplierDTO = assignRule.getAssignSupplier(params);
            if (supplierDTO != null && supplierDTO.getSupplierId() != null) {
                //清空post,因为现在没有做供应商的岗位配置所以在供应商分配清楚岗位不然品牌方会收到待办
                bindPosts = Collections.emptyList();
                // 如果之前有供应商了，判断新旧两个供应商是否一样，如果不一样那把操作人删除
                if (CollectionUtils.isNotEmpty(bindSuppliers)) {
                    String oldSupplierId = String.valueOf(bindSuppliers.get(0).getSupplierId());
                    String newSupplierId = supplierDTO.getSupplierId();
                    if (!StringUtils.equals(oldSupplierId, newSupplierId)) {
                        log.info("setNodeUserId regularDistribution null nodeId:{}, setProcessedBy:{}", id, null);
                        processedBy = null;
                    }
                }
                Supplier supplier = new Supplier();
                supplier.setSupplierId(Long.valueOf(supplierDTO.getSupplierId()));
                supplier.setSupplierType(supplierDTO.getSupplierType());
                supplier.setSupplierName(supplierDTO.getSupplierName());
                bindSuppliers = Collections.singletonList(supplier);
            } else {
                bindSuppliers = Collections.emptyList();
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
                    log.info("setNodeUserId regularFlowInnerOperator nodeId:{}, setProcessedBy:{}", id, node.getProcessedBy());
                    this.processedBy = node.getProcessedBy();
                    this.processedTime = LocalDateTime.now();
                    this.claimTime = LocalDateTime.now();
                });
    }

    public NotifyDTO toNotifyNormal(Flow flow) {
        NotifyDTO ret = new NotifyDTO();
        ret.setNodeId(id);
        ret.setNodeName(name);
        ret.setNodeVO(toNodeVo(flow));
        ret.setFlowId(flowId == null ? flow.getId() : flowId);
        ret.setTodoNotify(todoNotify);
        if (form != null && CollectionUtils.isNotEmpty(form.getFields())) {
            ret.setFiledList(form.getFields().stream().map(Field::getCode).collect(Collectors.toList()));
        }

        ret.setSupervisorIds(supervisorIds);
        ret.setCode(flow.getTemplateCode());
        ret.setPostIdList(postIdList());
        ret.setUserId(processedBy);
        ret.setMainFlowId(flow.mainFlowId());
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
        ret.setMainFlowId(flow.mainFlowId());
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
        ret.setMainFlowId(flow.mainFlowId());
        ret.setType(NotifyTypeEnum.DELETE);
        ret.setCode(flow.getTemplateCode());
        ret.setTenantId(getTenantId());

        return ret;
    }

    public NotifyDTO toNotifyCC(Flow flow, String remark) {
        return toNotifyCC(flow, remark, null);
    }

    public NotifyDTO toNotifyCC(Flow flow, String remark, Long executorId) {
        NotifyDTO ret = new NotifyDTO();
        ret.setNodeId(id);
        ret.setNodeName(name);
        ret.setNodeVO(toNodeVo(flow));
        ret.setTodoNotify(todoNotify);
        ret.setFlowId(flowId);
        ret.setUserId(processedBy);
        ret.setExecutorId(executorId);
        ret.setMainFlowId(flow.mainFlowId());
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

    public List<Long> postIdLongList() {
        if (CollectionUtils.isEmpty(bindPosts)) {
            return Collections.emptyList();
        }
        return bindPosts.stream()
            .map(Post::getPostId)
            .map(Long::valueOf)
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
     * 判断节点是否已认领，待被执行
     *
     * @return
     */
    public boolean isNotBeExecuted() {
        return status == NodeStatusEnum.ACTIVE || status == NodeStatusEnum.TO_BE_CLAIMED || status == NodeStatusEnum.NOT_ACTIVE;
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
            return isPostMatch(postIds, userId) || isSupplierMatch(supplierId);
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
    public boolean isPostMatch(List<Long> postIds, Long userId) {
        if (CollectionUtils.isEmpty(bindPosts)) {
            return false;
        }
        if (bindPosts.stream().anyMatch(post -> "-1".equals(post.getPostId()))) {
            return true;
        }
        if (CollectionUtils.isNotEmpty(claimableUserIds)) {
            return claimableUserIds.contains(userId);
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
        data.setNodeType(type.getCode());
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
        data.setFlowAutomateRuleName(flowAutomateRuleName);
        if (status != null) {
            data.setStatus(status.getCode());
        }
        data.setCustomStatus(customStatus);
        if (form == null || CollectionUtils.isEmpty(form.getFields())) {
            FlowRepository flowRepository = SpringUtils.getBean(FlowRepository.class);
            Form tempForm = flowRepository.repariForm(flow, form, name);
            Optional.ofNullable(tempForm).ifPresent(e -> data.setForm(e.toForm()));
        } else {
            Optional.ofNullable(form).ifPresent(e -> data.setForm(e.toForm()));
        }
        data.setProcessedBy(processedBy);
        data.setTodoNotify(todoNotify == null ? null : todoNotify.getCode());
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
        data.setClaimableUserIds(claimableUserIds);
        data.setProcessedTime(processedTime);
        data.setActiveTime(activeTime);
        data.setClaimTime(claimTime);
        data.setFinishTime(finishTime);
        if (flow != null) {
            data.setCode(flow.getTemplateCode());
            data.setMainFlowId(flow.mainFlowId());
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
            if (CollectionUtils.isNotEmpty(form.getFields())) {
                newForm.setFields(form.getFields().stream().map(Field::deepCopy).collect(Collectors.toList()));
            }
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
        node.claimableUserIds = claimableUserIds;
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
        node.flowAutomateRule = flowAutomateRule;
        node.claimTime = claimTime;
        node.finishTime = finishTime;
        node.activeTime = activeTime;
        return node;
    }


//    @Override
//    public Long id() {
//        return id;
//    }
//
//    @Override
//    public LocalDateTime taskCreateTime() {
//        return activeTime;
//    }
//
//    @Override
//    public LocalDateTime taskEndTime() {
//        return finishTime;
//    }
//
//    @Override
//    public LocalDateTime taskClaimTime() {
//        return claimTime;
//    }
//
//    @Override
//    public Duration getWorkTimeInMillis() {
//        if (finishTime == null || claimTime == null) {
//            return null;
//        }
//        return Duration.between(finishTime, claimTime);
//    }
//
//    @Override
//    public Duration getDurationInMillis() {
//        if (finishTime == null || activeTime == null) {
//            return null;
//        }
//        return Duration.between(finishTime, activeTime);
//    }


}
