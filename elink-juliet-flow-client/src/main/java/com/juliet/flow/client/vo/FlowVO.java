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
import javax.xml.soap.Node;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

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
        });
        // 说明该流程为异常流程，不允许变更
        if (parentId != 0) {
            return executor;
        }

        boolean canChange = userDoneNodeList.stream()
            .anyMatch(nodeVO -> subFlowList.stream()
                .allMatch(flowVO -> flowVO.nodeIsHandled(nodeVO.getId()))
            );
        executor.setCanChange(canChange);
        return executor;
    }

    public Boolean nodeIsHandled(Long nodeId) {
        NodeVO node = nodes.stream()
            .filter(nodeVO -> Objects.equals(nodeVO.getId(), nodeId))
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
