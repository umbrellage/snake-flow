package com.juliet.flow.client.vo;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.common.OperateTypeEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * FlowVO
 *
 * @author Geweilang
 * @date 2023/5/11
 */
@Getter
@Setter
public class FlowVO {

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
     * IN_PROGRESS(1, "进行中"), ABNORMAL(2, "异常中"), END(3, "已结束"),
     */
    private Integer status;
    /**
     * 最后操作人，流程未结束时最后操作人为空
     */
    private List<Long> theLastProcessedBy;

    private List<FlowVO> subFlowList;

/*    public List<String> getFlowCustomerStatus() {
        return this.flowCustomerStatus();
    }

    private List<String> flowCustomerStatus;*/

    /**
     * @param userId
     * @param postIdList
     * @return 1. 可办
     */
    public UserExecutor userExecutorInfo(Long userId, List<Long> postIdList) {
        UserExecutor executor = new UserExecutor();
        List<NodeVO> userDoneNodeList = new ArrayList<>();
        nodes.forEach(nodeVO -> {
            // 可编辑：
            // 1. 当前用户所属节点已经激活为可编辑
            if (Objects.equals(nodeVO.getProcessedBy(), userId) && (nodeVO.getStatus() == 3)) {
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
            // 未来可办
            // 1. 节点操作人为该用户，但未被激活
            if (Objects.equals(nodeVO.getProcessedBy(), userId) && (nodeVO.getStatus() == 1)) {
                executor.setWillEdit(true);
            }
            // 2. 用户属于该节点的岗位下，但未激活
            if (samePostId && nodeVO.getStatus() == 1) {
                executor.setWillEdit(true);
            }
            if (nodeVO.getStatus() == 4 && Objects.equals(nodeVO.getProcessedBy(), userId)) {
                userDoneNodeList.add(nodeVO);
            }
            // 当前可以操作
            if (nodeVO.getStatus() == 3 && Objects.equals(nodeVO.getProcessedBy(), userId)) {
                executor.setCurrentOperator(true);
            }
            // 调整操作人
            executor.setAdjustOperator(adjustOperator(userId));
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

        boolean adjustOperator= subFlowList.stream().anyMatch(subFlow -> subFlow.adjustOperator(userId)) || executor.getAdjustOperator();
        executor.setAdjustOperator(adjustOperator);
        boolean canChange = userDoneNodeList.stream()
            .anyMatch(nodeVO -> subFlowList.stream()
                .allMatch(flowVO -> flowVO.nodeIsHandled(nodeVO.getName()))
            );
        executor.setCanChange(canChange);
        return executor;
    }

    public boolean adjustOperator(Long userId) {
        return nodes.stream()
            .filter(nodeVO -> nodeVO.getStatus() == 2 || nodeVO.getStatus() == 3)
            .filter(nodeVO -> CollectionUtils.isNotEmpty(nodeVO.getSupervisorIds()))
            .anyMatch(nodeVO -> nodeVO.getSupervisorIds().contains(userId));
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
     * @return
     */
    public List<Long> processedBy() {
        if (CollectionUtils.isNotEmpty(subFlowList)) {
            subFlowList.add(this);
            return subFlowList.stream().map(FlowVO::getNodes)
                .flatMap(Collection::stream)
                .filter(nodeVO -> nodeVO.getStatus() == 3)
                .map(NodeVO::getProcessedBy)
                .distinct()
                .collect(Collectors.toList());
        }
        return nodes.stream()
            .filter(nodeVO -> nodeVO.getStatus() == 3)
            .map(NodeVO::getProcessedBy)
            .distinct()
            .collect(Collectors.toList());
    }


    public List<NodeSimpleVO> nodeList() {

        return nodes.stream()
            .map(NodeVO::toSimple)
            .collect(Collectors.toList());
    }

}
