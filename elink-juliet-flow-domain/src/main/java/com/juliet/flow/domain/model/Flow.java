package com.juliet.flow.domain.model;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.dto.NotifyDTO;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
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
    // TODO: 2023/6/15 许剑杰
    private String templateCode;

    private List<Node> nodes;

    private FlowStatusEnum status;

    private Long tenantId;

    /**
     * 流程是否已经结束
     *
     * @return
     */
    public boolean isFlowEnd() {
        return status == FlowStatusEnum.END || isEnd();
    }

    /**
     * 是否是异常子流程
     *
     * @return
     */
    public boolean hasParentFlow() {
        return parentId != null && parentId != 0;
    }


    /**
     * 当前流程节点是否已经结束
     */
    public boolean isEnd() {
        if (CollectionUtils.isEmpty(nodes)) {
            throw new ServiceException("流程不存在任何节点", StatusCode.SERVICE_ERROR.getStatus());
        }
        return nodes.stream().allMatch(
            node -> node.getStatus() == NodeStatusEnum.PROCESSED || node.getStatus() == NodeStatusEnum.IGNORE);
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


    public Node findTodoNode(Long userId) {
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        return nodes.stream()
            .filter(node -> userId.equals(node.getProcessedBy()) && node.isTodoNode())
            .findAny()
            .orElse(null);
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

    public Node findNodeThrow(String name) {
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        return nodes.stream()
            .filter(node -> node.getName().equals(name)).findAny()
            .orElseThrow(() -> new ServiceException("找不到节点"));
    }

    public Node startNode() {
        if (CollectionUtils.isNotEmpty(nodes)) {
            return nodes.stream()
                .filter(node -> node.getType().equals(NodeTypeEnum.START))
                .findAny()
                .orElseThrow(() -> new ServiceException("找不到开始节点"));
        }
        throw new ServiceException("节点为空");
    }

    /**
     * 前置节点是否已经处理
     *
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
        }
    }

    /**
     * 给节点分配一个待办人
     *
     * @param nodeName
     * @param userId
     */
    public void claimNode(String nodeName, Long userId) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        Node currentNode = findNode(nodeName);
        List<String> preNameList = currentNode.preNameList();
        boolean preHandled = nodes.stream().filter(node -> preNameList.contains(node.getName()))
            .allMatch(node -> node.getStatus() == NodeStatusEnum.PROCESSED);
        if (preHandled && currentNode.getStatus() == NodeStatusEnum.TO_BE_CLAIMED) {
            currentNode.setProcessedBy(userId);
            currentNode.setStatus(NodeStatusEnum.ACTIVE);
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
                .map(e -> e.toNodeVo(this))
                .collect(Collectors.toList());
            data.setNodes(nodeVOList);
        }
        data.setStatus(status.getCode());

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
        flow.setCreateTime(new Date());
        flow.setUpdateTime(new Date());
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
//            if (nextNameList.contains(node.getName()) && node.getStatus() == NodeStatusEnum.PROCESSED) {
            if (nextNameList.contains(node.getName()) && node.getStatus() != NodeStatusEnum.NOT_ACTIVE) {

                node.setStatus(NodeStatusEnum.NOT_ACTIVE);
                toBeProcessedNodeList.add(node);
            }
        });
        for (Node node : toBeProcessedNodeList) {
            modifyNodeStatus(node);
        }
    }

    /**
     * 递归设置节点状态为忽略
     *
     * @param ignoreNode 需要被忽略的节点
     */
    public void ignoreEqualAfterNode(Node ignoreNode) {
        ignoreNode.setStatus(NodeStatusEnum.IGNORE);
        List<String> nextNameList = ignoreNode.nextNameList();
        List<Node> ignoreNodeList = nextNameList.stream()
            .map(this::findNode)
            .filter(node -> node.preNameList().stream()
                .map(this::findNode)
                .allMatch(preNode -> preNode.getStatus() == NodeStatusEnum.IGNORE))
            .distinct()
            .collect(Collectors.toList());

        for (Node node : ignoreNodeList) {
            ignoreEqualAfterNode(node);
        }
    }

    /**
     * 通知待办列表
     *
     * @return
     */
    public List<NotifyDTO> normalNotifyList() {
        if (CollectionUtils.isEmpty(nodes)) {
            return Collections.emptyList();
        }
        return nodes.stream()
            .filter(Node::isTodoNode)
            .map(node -> node.toNotifyNormal(this))
            .collect(Collectors.toList());
    }

    /**
     * 异常通知列表
     *
     * @return
     */
    public List<NotifyDTO> anomalyNotifyList() {
        if (CollectionUtils.isEmpty(nodes)) {
            return Collections.emptyList();
        }
        return nodes.stream()
            .filter(Node::isToBeExecuted)
            .map(node -> node.toNotifyCC(this, "存在变更流程，您可以继续提交，也可以等待"))
            .collect(Collectors.toList());
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
                    .allMatch(handledNode -> handledNode.getStatus() == NodeStatusEnum.PROCESSED
                        || handledNode.getStatus() == NodeStatusEnum.IGNORE);
                // 如果需要激活的节点的前置节点都已经完成，节点才可以激活
                if (preHandled) {
                    // FIXME: 2023/6/15 传入参数
                    if (node.getAccessRule() != null) {
                        boolean flag = node.getAccessRule().accessRule(Collections.emptyMap());
                        // 如果规则不匹配，递归修改后面节点的状态为忽略
                        if (!flag) {
                            ignoreEqualAfterNode(node);
                        }
                    }
                    if (node.getType() == NodeTypeEnum.END) {
                        node.setStatus(NodeStatusEnum.PROCESSED);
                        return;
                    }
                    if (node.getStatus() == NodeStatusEnum.NOT_ACTIVE) {
                        // FIXME: 2023/6/16 传入参数
                        node.regularDistribution(Collections.emptyMap());
                        if (node.nodeTodo()) {
                            node.setStatus(NodeStatusEnum.ACTIVE);
                        } else {
                            node.setStatus(NodeStatusEnum.TO_BE_CLAIMED);
                        }
                        return;
                    }
                    if (node.getStatus() == NodeStatusEnum.PROCESSED) {
                        node.setStatus(NodeStatusEnum.ACTIVE);
                    }
                }
            }
        });
    }

    /**
     * 判断该节点在当前流程中与该节点的相同的节点是否已完成
     *
     * @param nodeName 节点名称
     * @return true 可以创建异常流程， false 不可以
     */
    public boolean checkoutFlowNodeIsHandled(String nodeName) {
        Node node = findNodeThrow(nodeName);
        return node.getStatus() == NodeStatusEnum.PROCESSED;
    }

    /**
     * 校准流程节点,并且返回需要被通知的待办
     *
     * @param flow 标准流程，按照这个流程来校准
     * @return 需要被通知的消息待办
     */
    public List<NotifyDTO> calibrateFlow(Flow flow) {
        List<NotifyDTO> notifyNodeList = new ArrayList<>();
        Map<String, Node> nodeMap = flow.getNodes().stream()
            .collect(Collectors.toMap(Node::getName, Function.identity()));
        nodes.forEach(node -> {
            Node standardNode = nodeMap.get(node.getName());
            if (standardNode.getStatus() == NodeStatusEnum.IGNORE && node.getStatus() != NodeStatusEnum.IGNORE) {
                if (node.getStatus() == NodeStatusEnum.ACTIVE) {
                    NotifyDTO cc = node.toNotifyCC(this, "已不会流经该节点，您不需要再处理该节点, 已将您的待办删除");
                    notifyNodeList.add(cc);
                    NotifyDTO delete = node.toNotifyDelete(this);
                    notifyNodeList.add(delete);
                }
                node.setStatus(NodeStatusEnum.IGNORE);
            }
            if (standardNode.getStatus() != NodeStatusEnum.IGNORE && node.getStatus() == NodeStatusEnum.IGNORE) {
                node.setStatus(NodeStatusEnum.PROCESSED);
            }
        });

        return notifyNodeList;
    }
}
