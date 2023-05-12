package com.juliet.flow.domain.model;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
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
import org.springframework.beans.BeanUtils;

/**
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

    public boolean isFlowEnd() {
        return status == FlowStatusEnum.END || isEnd();
    }

    /**
     * 当前流程是否已经结束
     */
    public boolean isEnd() {
        if (CollectionUtils.isEmpty(nodes)) {
            throw new ServiceException("流程不存在任何节点", StatusCode.SERVICE_ERROR.getStatus());
        }
        boolean end = nodes.stream().allMatch(node -> node.getStatus() == NodeStatusEnum.PROCESSED);
        if (end) {
            this.status = FlowStatusEnum.END;
        }
        return end;
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
     *
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

    public Node findNode(Long nodeId) {
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        return nodes.stream()
            .filter(node -> node.getId().equals(nodeId)).findAny()
            .orElse(null);
    }

    /**
     * 给节点分配一个待办人
     *
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

    public FlowVO flowVO() {
        FlowVO data = new FlowVO();
        data.setId(id);
        data.setName(name);
        data.setParentId(parentId);
        data.setFlowTemplateId(flowTemplateId);
        data.setTenantId(tenantId);
        if (CollectionUtils.isNotEmpty(nodes)) {
            List<NodeVO> nodeVOList = nodes.stream()
                .map(e -> e.toNodeVo(id))
                .collect(Collectors.toList());
            data.setNodes(nodeVOList);
        }

        return data;
    }

    public Flow subFlow() {
        Flow flow = new Flow();
        // TODO: 2023/5/11
        flow.setId(null);
        flow.setName(name);
        flow.setFlowTemplateId(flowTemplateId);
        flow.setParentId(id);
        List<Node> nodeList = nodes.stream()
            .map(Node::copyNode)
            .collect(Collectors.toList());
        flow.setNodes(nodeList);
        flow.setStatus(FlowStatusEnum.ABNORMAL);
        flow.setTenantId(tenantId);
        return flow;
    }

    /**
     * 递归修改已处理节点的状态，修改为已认领
     *
     */
    public void modifyNodeStatus(Node errorNode) {
        List<Node> toBeProcessedNodeList = new ArrayList<>();
        nodes.forEach(node -> {
            //如果当前节点的节点名称等于错误节点的下一节点名称，且当前节点的节点状态为已处理，则修改当前节点的节点状态为已认领
            if (errorNode.getNextName().equals(node.getName()) && node.getStatus() == NodeStatusEnum.PROCESSED) {
                node.setStatus(NodeStatusEnum.ACTIVE);
                toBeProcessedNodeList.add(node);
            }
        });
        for (Node node: toBeProcessedNodeList) {
            modifyNodeStatus(node);
        }
    }

    /**
     * 完成某个节点的处理
     * @param nodeId
     */
    public void finishNode(Long nodeId) {
        nodes.stream()
            .filter(node -> node.getId().equals(nodeId))
            .forEach(node -> node.setStatus(NodeStatusEnum.PROCESSED));

    }
}
