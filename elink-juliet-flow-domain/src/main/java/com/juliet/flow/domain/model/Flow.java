package com.juliet.flow.domain.model;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;

import java.util.Collection;
import java.util.List;

/**
 *
 * 流程
 *
 * @author xujianjie
 * @date 2023-05-06
 */
@Data
public class Flow extends BaseModel {

    private Long id;

    private String name;
    /**
     * 当流程为子流程时，存在父流程
     */
    private Long parentId;

    private Long flowTemplateId;

    private List<Node> nodes;

    private FlowStatusEnum status;

    private Long tenantId;

    public boolean forward() {
        return true;
    }

    public Todo getCurrentTodo() {
        return new Todo();
    }

    /**
     * 当前流程是否已经结束
     */
    public boolean isEnd() {
        if (CollectionUtils.isEmpty(nodes)) {
            throw new ServiceException("流程不存在任何节点", StatusCode.SERVICE_ERROR.getStatus());
        }
        return nodes.stream().allMatch(node -> node.getStatus() == NodeStatusEnum.PROCESSED);
    }

    /**
     * 返回当前流程中处理的节点
     */
    private Node getCurrentActiveNode() {
        return new Node();
    }

    public void validate() {
        BusinessAssert.assertNotEmpty(this.nodes, StatusCode.SERVICE_ERROR, "不能没有节点信息!");
    }

    /**
     * 图遍历，获取节点
     * @param nodeStatusList 节点状态
     * @return 节点列表
     */
    public List<Node> getNodeByNodeStatus(List<NodeStatusEnum> nodeStatusList) {
        if (CollectionUtils.isEmpty(nodeStatusList) || CollectionUtils.isEmpty(nodes)) {
            return Collections.emptyList();
        }
        return nodes.stream()
            .filter(node -> nodeStatusList.contains(node.getStatus()))
            .collect(Collectors.toList());
    }

    /**
     * 给节点分配一个待办人
     * @param nodeId
     * @param userId
     */
    public void claimNode(Long nodeId, Long userId) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        nodes.stream()
            .filter(node -> node.getId().equals(nodeId))
            .forEach(node -> node.setProcessedBy(userId));
    }
}
