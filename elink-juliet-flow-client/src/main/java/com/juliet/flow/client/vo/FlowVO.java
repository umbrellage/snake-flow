package com.juliet.flow.client.vo;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.common.NodeStatusEnum;
import com.juliet.flow.client.common.TodoNotifyEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * FlowVO
 *
 * @author Geweilang
 * @date 2023/5/11
 */
@Getter
@Setter
public class FlowVO implements Serializable {

    private Long id;

    private String name;
    /**
     * 当流程为子流程时，存在父流程
     */
    private Long parentId;

    private Long flowTemplateId;

    private List<NodeVO> nodes;

    private Long tenantId;

    private Boolean hasSubFlow;
    /**
     * 异常流程数量
     */
    private Integer subFlowCount;

    /**
     * IN_PROGRESS(1, "进行中"), ABNORMAL(2, "异常中"), END(3, "已结束"),4,已作废
     */
    private Integer status;
    /**
     * 最后操作人，流程未结束时最后操作人为空
     */
    private List<Long> theLastProcessedBy;

    private List<FlowVO> subFlowList = new ArrayList<>();

/*    public List<String> getFlowCustomerStatus() {
        return this.flowCustomerStatus();
    }

    private List<String> flowCustomerStatus;*/

    public UserExecutor userExecutorInfo(Long userId, List<Long> postIdList, Long supplierId) {
        return userExecutorInfo(Collections.singletonList(userId), postIdList, supplierId);
    }


    public UserExecutor userExecutorInfo(List<Long> userIdList, List<Long> postIdList, Long supplierId) {
        UserExecutor executor = new UserExecutor();
        List<NodeVO> userDoneNodeList = new ArrayList<>();
        List<NodeVO> allNodeList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subFlowList)) {
            allNodeList = subFlowList.stream()
                    .map(FlowVO::getNodes)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }

        allNodeList.addAll(nodes);
        allNodeList.forEach(nodeVO -> {
            // 可编辑：
            // 1. 当前用户所属节点已经激活为可编辑
            if (userIdList.contains(nodeVO.getProcessedBy()) && (nodeVO.getStatus() == 3)) {
                executor.setCanEdit(true);
            }
            // 2. 属于该岗位下，节点未被认领
            List<Long> postIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(nodeVO.getBindPosts())) {
                postIds = nodeVO.getBindPosts().stream()
                        .map(PostVO::getPostId)
                        .filter(Objects::nonNull)
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
            }
            boolean samePostId = !Collections.disjoint(postIdList, postIds) || postIds.stream().anyMatch(postId -> postId == -1);
            if (samePostId && nodeVO.getStatus() == 2) {
                executor.setCanEdit(true);
            }
            // 3.属于该供应商的节点，节点未被认领
            List<Long> supplierIdList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(nodeVO.getBindSuppliers())) {
                supplierIdList = nodeVO.getBindSuppliers().stream()
                        .map(SupplierVO::getSupplierId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
            boolean isSupplier = supplierIdList.contains(supplierId);
            if (isSupplier && nodeVO.getStatus() == 2 && supplierId != null) {
                executor.setCanEdit(true);
            }

            // 未来可办
            // 1. 节点操作人为该用户，但未被激活
            if (userIdList.contains(nodeVO.getProcessedBy()) && (nodeVO.getStatus() == 1)) {
                executor.setWillEdit(true);
            }
            // 2. 用户属于该节点的岗位下，但未激活
            if (samePostId && nodeVO.getStatus() == 1) {
                executor.setWillEdit(true);
            }
            if (nodeVO.getStatus() == 4 && userIdList.contains(nodeVO.getProcessedBy())) {
                userDoneNodeList.add(nodeVO);
            }
            // 当前可以操作
            if (nodeVO.getStatus() == 3 && userIdList.contains(nodeVO.getProcessedBy())) {
                executor.setCurrentOperator(true);
            }
            // 调整操作人
            executor.setAdjustOperator(adjustOperatorAnyMatch(userIdList));
        });
        // 说明该流程为异常流程或者不存在变更的节点，则不允许变更
//        executor.setCanChange(nodes.stream().anyMatch(nodeVO -> nodeVO != null && nodeVO.getStatus() == 4 && nodeVO.getProcessedBy().equals(userId)));
        if (parentId != 0 || CollectionUtils.isEmpty(userDoneNodeList)) {
            return executor;
        }

        if (CollectionUtils.isEmpty(subFlowList)) {
            executor.setCanChange(true);
            return executor;
        }

        boolean adjustOperator = subFlowList.stream().anyMatch(subFlow -> subFlow.adjustOperatorAnyMatch(userIdList)) || executor.getAdjustOperator();
        executor.setAdjustOperator(adjustOperator);
        boolean canChange = userDoneNodeList.stream()
                .anyMatch(nodeVO -> subFlowList.stream()
                        .allMatch(flowVO -> flowVO.nodeIsHandled(nodeVO.getName()))
                );
        executor.setCanChange(canChange);
        return executor;
    }


    /**
     * @param userId
     * @param postIdList
     * @return 1. 可办
     */
    public UserExecutor userExecutorInfo(Long userId, List<Long> postIdList) {
        return userExecutorInfo(userId, postIdList, null);
    }

    /**
     * 可分配
     *
     * @param userId
     * @return
     */
    public boolean adjustOperator(Long userId) {
        return nodes.stream()
                .filter(nodeVO -> nodeVO.getStatus() == 2 || nodeVO.getStatus() == 3)
                .filter(nodeVO -> CollectionUtils.isNotEmpty(nodeVO.getSupervisorIds()))
                .anyMatch(nodeVO -> nodeVO.getSupervisorIds().contains(userId));
    }

    public boolean adjustOperatorAnyMatch(List<Long> userIdList) {
        return nodes.stream()
                .filter(nodeVO -> nodeVO.getStatus() == 2 || nodeVO.getStatus() == 3)
                .filter(nodeVO -> CollectionUtils.isNotEmpty(nodeVO.getSupervisorIds()))
                .anyMatch(nodeVO -> Collections.disjoint(nodeVO.getSupervisorIds(), userIdList));
    }

    public Boolean nodeIsHandled(String nodeName) {
        NodeVO node = nodes.stream()
                .filter(nodeVO -> StringUtils.equals(nodeVO.getName(), nodeName))
                .findAny()
                .orElseThrow(() -> new ServiceException("找不到节点"));
        return node.getStatus() == 4;
    }


    public boolean end() {
        return status == 3;
    }

    @Deprecated
    public List<String> flowCustomerStatus() {
        if (end()) {
            return Collections.singletonList(nodes.get(nodes.size() - 1).getCustomStatus());
        }
        if (CollectionUtils.isNotEmpty(subFlowList)) {
            subFlowList.add(this);
            return subFlowList.stream().map(FlowVO::getNodes)
                    .flatMap(Collection::stream)
                    .filter(nodeVO -> nodeVO.getStatus() == 3 || nodeVO.getStatus() == 2)
                    .map(NodeVO::getCustomStatus)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return nodes.stream()
                .filter(nodeVO -> nodeVO.getStatus() == 3 || nodeVO.getStatus() == 2)
                .map(NodeVO::getCustomStatus)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 只有主流程
     *
     * @return
     */
    public List<NodeVO> currentNode() {
        return nodes.stream()
                .filter(nodeVO -> nodeVO.getStatus() == 3 || nodeVO.getStatus() == 2)
                .collect(Collectors.toList());
    }

    /**
     * 当前操作人
     */
    public List<Long> processedBy() {
        List<FlowVO> allFlowList = allFlowList();
        return allFlowList.stream().map(FlowVO::getNodes)
                .flatMap(Collection::stream)
                .filter(nodeVO -> nodeVO.getStatus() == 3)
                .map(NodeVO::getProcessedBy)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 当前可以认领的岗位
     */
    public List<Long> tobeClaimedPost() {
        List<FlowVO> allFlowList = allFlowList();
        return allFlowList.stream().map(FlowVO::getNodes)
                .flatMap(Collection::stream)
                .filter(nodeVO -> CollectionUtils.isNotEmpty(nodeVO.getBindPosts()) && Objects.equals(nodeVO.getStatus(), NodeStatusEnum.TO_BE_CLAIMED.getCode()))
                .map(NodeVO::getBindPosts)
                .flatMap(Collection::stream)
                .filter(postVO -> postVO != null && StringUtils.isNotBlank(postVO.getPostId()))
                .map(postVO -> Long.valueOf(postVO.getPostId()))
                .filter(postId -> postId > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 节点已经被认领的岗位
     */
    public List<Long> notTobeClaimedPost() {
        List<FlowVO> allFlowList = allFlowList();
        return allFlowList.stream().map(FlowVO::getNodes)
                .flatMap(Collection::stream)
                .filter(nodeVO -> CollectionUtils.isNotEmpty(nodeVO.getBindPosts()) && !Objects.equals(nodeVO.getStatus(), NodeStatusEnum.TO_BE_CLAIMED.getCode()))
                .map(NodeVO::getBindPosts)
                .flatMap(Collection::stream)
                .filter(postVO -> postVO != null && StringUtils.isNotBlank(postVO.getPostId()))
                .map(postVO -> Long.valueOf(postVO.getPostId()))
                .filter(postId -> postId > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 待办列表
     */
    public List<NodeVO> todoNodeList() {
        return nodeList(TodoNotifyEnum.NOTIFY);
    }

    /**
     * 可办列表
     */
    public List<NodeVO> canDoNodeList() {
        return nodeList(TodoNotifyEnum.NO_NOTIFY);
    }

    /**
     * 完成/已忽略的节点
     */
    public List<NodeVO> doneNodeList() {
        List<FlowVO> allFlowList = allFlowList();
        return allFlowList.stream().map(FlowVO::getNodes)
                .flatMap(Collection::stream)
                .filter(nodeVO -> nodeVO != null && nodeVO.getTodoNotify() != null && nodeVO.getStatus() != null &&
                        Arrays.asList(NodeStatusEnum.PROCESSED.getCode(), NodeStatusEnum.IGNORE.getCode()).contains(nodeVO.getStatus()))
                .collect(collectingAndThen(toCollection(() ->
                        new TreeSet<>(Comparator.comparing(NodeVO::distinct))), ArrayList::new));
    }

    /**
     * 不活跃的节点，不可办的节点
     */
    public List<NodeVO> notActiveNodeList() {
        List<FlowVO> allFlowList = allFlowList();
        return allFlowList.stream().map(FlowVO::getNodes)
                .flatMap(Collection::stream)
                .filter(nodeVO -> nodeVO != null && nodeVO.getTodoNotify() != null && nodeVO.getStatus() != null &&
                        Arrays.asList(NodeStatusEnum.PROCESSED.getCode(), NodeStatusEnum.IGNORE.getCode(), NodeStatusEnum.NOT_ACTIVE.getCode()).contains(nodeVO.getStatus()))
                .collect(collectingAndThen(toCollection(() ->
                        new TreeSet<>(Comparator.comparing(NodeVO::distinct))), ArrayList::new));
    }

    private List<NodeVO> nodeList(TodoNotifyEnum todoNotify) {
        List<FlowVO> allFlowList = allFlowList();
        return allFlowList.stream().map(FlowVO::getNodes)
                .flatMap(Collection::stream)
                .filter(nodeVO -> nodeVO != null && nodeVO.getTodoNotify() != null && nodeVO.getStatus() != null &&
                        Objects.equals(nodeVO.getTodoNotify(), todoNotify.getCode()) &&
                        Arrays.asList(NodeStatusEnum.ACTIVE.getCode(), NodeStatusEnum.TO_BE_CLAIMED.getCode()).contains(nodeVO.getStatus()))
                .collect(collectingAndThen(toCollection(() ->
                        new TreeSet<>(Comparator.comparing(NodeVO::distinct))), ArrayList::new));
    }

    private List<FlowVO> allFlowList() {
        List<FlowVO> allFlowList = new ArrayList<>();
        allFlowList.add(this);
        if (CollectionUtils.isNotEmpty(subFlowList)) {
            allFlowList.addAll(subFlowList);
        }
        return allFlowList;
    }


    public List<NodeSimpleVO> nodeList() {

        return nodes.stream()
                .map(NodeVO::toSimple)
                .collect(Collectors.toList());
    }

}
