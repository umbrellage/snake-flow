package com.juliet.flow.domain.model;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import java.util.List;

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
        return nodes.stream().allMatch(node -> node.getStatus() == NodeStatusEnum.PROCESSED);
    }

    /**
     * 根据可填写的字段查找节点
     *
     * @param body
     * @return
     */
    public Node findNode(Map<String, ?> body) {
        return nodes.stream().filter(node -> {
                Form form = node.getForm();
                List<String> codeList = form.getFields().stream()
                    .map(Field::getCode)
                    .collect(Collectors.toList());

                return codeList.containsAll(body.keySet()) && body.size() == codeList.size();
            })
            .findAny()
            .orElseThrow(() -> new ServiceException("提交的表单数据无法查询到相应的流程，请检查提交的参数"));
    }

    /**
     * 根据可填写的字段查找节点
     *
     * @param fieldCodeList
     * @return
     */
    public Node findNode(List<String> fieldCodeList) {
        return nodes.stream().filter(node -> {
                Form form = node.getForm();
                List<String> codeList = form.getFields().stream()
                    .map(Field::getCode)
                    .collect(Collectors.toList());

                return codeList.containsAll(fieldCodeList) && fieldCodeList.size() == codeList.size();
            })
            .findAny()
            .orElseThrow(() -> new ServiceException("提交的表单数据无法查询到相应的流程，请检查提交的参数"));
    }



    public Node findNode(Long nodeId) {
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        return nodes.stream()
            .filter(node -> node.getId().equals(nodeId)).findAny()
            .orElse(null);
    }

    public Node findNodeThrow(Long nodeId) {
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        return nodes.stream()
            .filter(node -> node.getId().equals(nodeId)).findAny()
            .orElseThrow(() -> new ServiceException("找不到节点"));
    }

    public Node findNode(String name) {
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        return nodes.stream()
            .filter(node -> node.getName().equals(name)).findAny()
            .orElse(null);
    }

    /**
     * 前置节点已经处理
     * @param name
     * @return
     */
    public boolean ifPreNodeIsHandle(String name) {
        Node node = findNode(name);
        if (node == null) {
            throw new ServiceException("找不到节点");
        }
        return Arrays.stream(node.getPreName().split(",")).map(this::findNode).allMatch(Node::isProcessed);
    }


    public void validate() {
        BusinessAssert.assertNotEmpty(this.nodes, StatusCode.SERVICE_ERROR, "不能没有节点信息!");
    }

    /**
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
     *
     * @param nodeId
     * @param userId
     */
    public void claimNode(Long nodeId, Long userId) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        Node currentNode = findNodeThrow(nodeId);
        List<String> preNameList = currentNode.preNameList();
        boolean preHandled = nodes.stream().filter(node -> preNameList.contains(node.getName()))
            .allMatch(node -> node.getStatus() == NodeStatusEnum.PROCESSED);
        if (preHandled && currentNode.getStatus() == NodeStatusEnum.TO_BE_CLAIMED) {
            currentNode.setProcessedBy(userId);
            currentNode.setStatus(NodeStatusEnum.ACTIVE);
        } else {
            throw new ServiceException("流程未走到当前节点");
        }
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
     * 新建一条错误流程时 递归修改已处理节点的状态，修改为未激活
     */
    public void modifyNodeStatus(Node errorNode) {
        List<String> nextNameList = errorNode.nextNameList();
        List<Node> toBeProcessedNodeList = new ArrayList<>();
        nodes.forEach(node -> {
            //如果当前节点的节点名称等于错误节点的下一节点名称，且当前节点的节点状态为已处理，则修改当前节点的节点状态为未激活
            if (nextNameList.contains(node.getName()) && node.getStatus() == NodeStatusEnum.PROCESSED) {
                node.setStatus(NodeStatusEnum.NOT_ACTIVE);
                toBeProcessedNodeList.add(node);
            }
        });
        for (Node node : toBeProcessedNodeList) {
            modifyNodeStatus(node);
        }
    }

    /**
     * 完成某一节点的处理，并且修改下一节点的状态从待激活到待认领，如果处理人不为null则修改为已认领
     *
     * @param nodeId
     */
    public void modifyNextNodeStatus(Long nodeId) {
        Node currentNode = findNode(nodeId);
        if (currentNode == null) {
            return;
        }

        List<String> nextNameList = currentNode.nextNameList();
        nodes.forEach(node -> {
            if (node.getId().equals(nodeId)) {
                node.setStatus(NodeStatusEnum.PROCESSED);
            }
        });


        nodes.forEach(node -> {
            if (nextNameList.contains(node.getName())) {

                List<String> preNameList = node.preNameList();
                boolean preHandled = nodes.stream().filter(handledNode -> preNameList.contains(handledNode.getName()))
                    .allMatch(handledNode -> handledNode.getStatus() == NodeStatusEnum.PROCESSED);
                // 如果需要激活的节点的前置节点都已经完成，节点才可以激活
                if (preHandled) {
                    if (node.getStatus() == NodeStatusEnum.NOT_ACTIVE) {
                        if (node.getProcessedBy() != null) {
                            node.setStatus(NodeStatusEnum.ACTIVE);
                        }else {
                            node.setStatus(NodeStatusEnum.TO_BE_CLAIMED);
                        }
                    }
                    if (node.getStatus() == NodeStatusEnum.PROCESSED) {
                        node.setStatus(NodeStatusEnum.ACTIVE);
                    }
                }
            }
        });
    }
}
