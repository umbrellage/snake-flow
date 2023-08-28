package com.juliet.flow.domain.model;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.common.NotifyTypeEnum;
import com.juliet.flow.client.dto.NotifyDTO;
import com.juliet.flow.client.dto.RollbackDTO;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import com.juliet.flow.client.common.TodoNotifyEnum;
import com.juliet.flow.common.utils.BusinessAssert;

import com.juliet.flow.constant.FlowConstant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 流程
 *
 * @author xujianjie
 * @date 2023-05-06
 */
@Data
@Slf4j
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

    public List<Long> theLastProcessedBy() {
        if (FlowStatusEnum.END != status) {
            return Collections.emptyList();
        }
        Node end = nodes.stream()
            .filter(e -> e.getType() == NodeTypeEnum.END).findAny()
            .orElseThrow(() -> new ServiceException("不存在结束节点，流程异常，流程id：" + id));
        List<Node> nodeList = end.preNameList().stream()
            .map(this::findNode)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return nodeList.stream().map(Node::getProcessedBy).filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }

    public void reject() {
        nodes.forEach(node -> {
            if (node.getStatus() != NodeStatusEnum.PROCESSED && node.getStatus() != NodeStatusEnum.IGNORE) {
                node.setStatus(NodeStatusEnum.PROCESSED);
            }
        });
        status = FlowStatusEnum.END;
    }

    /**
     * 流程是否已经结束
     *
     * @return
     */
    public boolean isFlowEnd() {
        return status == FlowStatusEnum.END;
    }


    public List<History> forwardHistory(Long nodeId, Long userId) {
        Node currentNode = findNode(nodeId);
        if (currentNode == null) {
            return Collections.emptyList();
        }
        return currentNode.nextNameList().stream()
            .map(this::findNode)
            .map(node -> History.of(this, userId, nodeId, node.getId()))
            .collect(Collectors.toList());
    }


    public NotifyDTO invalidFlow() {
        NotifyDTO dto = new NotifyDTO();
        dto.setFlowId(id);
        dto.setTenantId(getTenantId());
        dto.setType(NotifyTypeEnum.INVALID);
        dto.setCode(getTemplateCode());
        return dto;
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
            node -> node.getStatus() == NodeStatusEnum.PROCESSED ||
                node.getStatus() == NodeStatusEnum.IGNORE ||
                node.getTodoNotify() == TodoNotifyEnum.NO_NOTIFY);
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
        Node currentNode = findNode(name);
        if (currentNode == null) {
            throw new ServiceException("找不到节点");
        }
        if (StringUtils.isBlank(currentNode.getPreName())) {
            return true;
        }

//        /*
//         * 判断当前节点的所有前置节点
//         */
//        return recursionGetPreNode(name).stream()
//            .allMatch(node -> node.getStatus() == NodeStatusEnum.PROCESSED ||
//                node.getStatus() == NodeStatusEnum.IGNORE ||
//                node.getTodoNotify() == TodoNotifyEnum.NO_NOTIFY);
        /*
         * 只判断当前节点的前一级节点
         */
        return Arrays.stream(currentNode.getPreName().split(","))
            .map(this::findNode)
            .allMatch(node -> node.getStatus() == NodeStatusEnum.PROCESSED ||
            node.getStatus() == NodeStatusEnum.IGNORE);
    }

    public List<Node> recursionGetPreNode(String name) {
        List<Node> nodeList = new ArrayList<>();
        Node node = findNode(name);
        LinkedList<Node> preNodeList = new LinkedList<>();
        preNodeList.add(node);
        while (CollectionUtils.isNotEmpty(preNodeList)) {
            Node currentNode = preNodeList.pollLast();
            assert currentNode != null;
            List<Node> nodes = getPreNode(currentNode);
            if (CollectionUtils.isNotEmpty(nodes)) {
                nodeList.addAll(nodes);
                preNodeList.addAll(nodes);
            }
        }
        return nodeList;
    }

    public List<Node> getPreNode(Node node) {
        return node.preNameList().stream()
            .map(this::findNode)
            .collect(Collectors.toList());
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
        boolean preHandled = ifPreNodeIsHandle(currentNode.getName());
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
        boolean preHandled = ifPreNodeIsHandle(nodeName);
        if (preHandled && currentNode.getStatus() == NodeStatusEnum.TO_BE_CLAIMED) {
            currentNode.setProcessedBy(userId);
            currentNode.setStatus(NodeStatusEnum.ACTIVE);
        } else {
            currentNode.setProcessedBy(userId);
        }
    }

    public FlowVO flowVO(List<FlowVO> subFlowList) {
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
        data.setHasSubFlow(CollectionUtils.isNotEmpty(subFlowList));
        data.setSubFlowCount(subFlowList == null ? 0 : subFlowList.size());
        data.setStatus(status.getCode());
        data.setSubFlowList(subFlowList);
        data.setTheLastProcessedBy(theLastProcessedBy());
        return data;
    }

    public FlowVO flowVO(List<FlowVO> subFlowList, List<History> historyList) {
        FlowVO data = flowVO(subFlowList);
        Map<Long, History> historyMap = historyList.stream()
            .filter(history -> history.getTargetNodeId() != null)
            .collect(Collectors.toMap(History::getTargetNodeId, Function.identity(),
                (x, y) -> x.getCreateTime().isAfter(y.getCreateTime()) ? x : y));
        data.getNodes().forEach(node -> {
            History history = historyMap.get(node.getId());
            if (history != null) {
                node.setRemark(history.getComment());
                node.setOperateType(history.getAction());
            }
        });
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

    public NotifyDTO flowEndNotify() {
        NotifyDTO dto = new NotifyDTO();
        dto.setFlowId(id);
        dto.setCode(templateCode);
        dto.setType(NotifyTypeEnum.END);
        return dto;
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

    public void activeNode(Node node, Map<String, Object> param) {
        boolean preHandled = ifPreNodeIsHandle(node.getName()) && (node.getActiveRule()== null || node.getActiveRule().activeSelf(this));
        // 如果需要激活的节点的前置节点都已经完成，节点才可以激活
        if (preHandled) {
            if (node.getAccessRule() != null) {
                param.put(FlowConstant.INNER_FLOW, this);
                param.put(FlowConstant.CURRENT_NODE, node);
                boolean flag = node.getAccessRule().accessRule(param);
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
                node.regularDistribution(param, this);
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

    /**
     * 完成某一节点的处理，并且修改下一节点的状态从待激活到待认领，如果处理人不为null则修改为已认领
     *
     * @param nodeId
     */
    public void modifyNextNodeStatus(Long nodeId, Map<String, Object> param) {
        Node currentNode = findNode(nodeId);
        if (currentNode == null) {
            return;
        }

        List<String> nextNameList = currentNode.nextNameList();
        nodes.forEach(node -> {
            if (node.getId().equals(nodeId)) {
                node.setStatus(NodeStatusEnum.PROCESSED);
                node.setUpdateTime(new Date());
            }
        });
        // 修改节点消息通知状态
        modifyNodeTodoStatusAndActiveSelf(this, param);
        nextNameList.stream()
            .map(this::findNode)
            .forEach(node -> activeNode(node, param));
//        nodes.forEach(node -> {
//            if (nextNameList.contains(node.getName())) {
//                boolean preHandled = ifPreNodeIsHandle(node.getName());
//                // 如果需要激活的节点的前置节点都已经完成，节点才可以激活
//                if (preHandled) {
//                    if (node.getAccessRule() != null) {
//                        param.put(FlowConstant.INNER_FLOW, this);
//                        param.put(FlowConstant.CURRENT_NODE, node);
//                        boolean flag = node.getAccessRule().accessRule(param);
//                        // 如果规则不匹配，递归修改后面节点的状态为忽略
//                        if (!flag) {
//                            ignoreEqualAfterNode(node);
//                        }
//                    }
//                    if (node.getType() == NodeTypeEnum.END) {
//                        node.setStatus(NodeStatusEnum.PROCESSED);
//                        return;
//                    }
//                    if (node.getStatus() == NodeStatusEnum.NOT_ACTIVE) {
//                        node.regularDistribution(param, this);
//                        if (node.nodeTodo()) {
//                            node.setStatus(NodeStatusEnum.ACTIVE);
//                        } else {
//                            node.setStatus(NodeStatusEnum.TO_BE_CLAIMED);
//                        }
//                        return;
//                    }
//                    if (node.getStatus() == NodeStatusEnum.PROCESSED) {
//                        node.setStatus(NodeStatusEnum.ACTIVE);
//                    }
//                }
//            }
//        });
    }

    public void modifyNodeTodoStatusAndActiveSelf(Flow flow, Map<String, Object> param) {
        nodes.stream()
            .filter(node -> node.getActiveRule() != null)
            .forEach(node -> {
                List<Long> nodeIdList = node.getActiveRule().notifyNodeIds(flow);
                nodeIdList.forEach(nodeId -> {
                    Node activeNode = findNode(nodeId);
                    if (activeNode == null) {
                        throw new ServiceException("node active rule configuration error, nodeId:" + nodeId);
                    }
                    activeNode.setTodoNotify(TodoNotifyEnum.NOTIFY);
                });

                boolean active = node.getActiveRule().activeSelf(flow);
                if (active) {
                    activeNode(node, param);
                    node.setStatus(NodeStatusEnum.TO_BE_CLAIMED);
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
     * @param standardFlow 标准流程，按照这个流程来校准
     * @return 需要被通知的消息待办
     */
    @Deprecated
    public List<NotifyDTO> calibrateFlow(Flow standardFlow) {
        List<NotifyDTO> notifyNodeList = new ArrayList<>();
        Map<String, Node> nodeMap = standardFlow.getNodes().stream()
            .collect(Collectors.toMap(Node::getName, Function.identity()));
        nodes.forEach(node -> {
            Node standardNode = nodeMap.get(node.getName());
            if (standardNode.getStatus() == NodeStatusEnum.IGNORE) {
                if (node.getStatus() == NodeStatusEnum.ACTIVE) {
                    NotifyDTO cc = node.toNotifyCC(this, "已不会流经该节点，您不需要再处理该节点, 已将您的待办删除");
                    notifyNodeList.add(cc);
                    NotifyDTO delete = node.toNotifyDelete(this);
                    notifyNodeList.add(delete);
                }
                node.setStatus(NodeStatusEnum.IGNORE);
            }
            if (standardNode.getStatus() != NodeStatusEnum.IGNORE) {
                if (node.getStatus() == NodeStatusEnum.IGNORE) {
                    node.setStatus(NodeStatusEnum.PROCESSED);
                }
            }
        });

        return notifyNodeList;
    }

    /**
     * 校准流程节点,并且返回需要被通知的节点
     *
     * @param standardFlow 标准流程，按照这个流程来校准
     * @return 需要被通知的节点
     */
    public List<Node> calibrateFlowV2(Flow standardFlow) {
        List<Node> notifyNodeList = new ArrayList<>();
        Map<String, Node> nodeMap = standardFlow.getNodes().stream()
            .collect(Collectors.toMap(Node::getName, Function.identity()));
        nodes.forEach(node -> {
            Node standardNode = nodeMap.get(node.getName());
            if (standardNode.getStatus() == NodeStatusEnum.IGNORE) {
                if (node.getStatus() == NodeStatusEnum.ACTIVE) {
                    notifyNodeList.add(node);
                }
                node.setStatus(NodeStatusEnum.IGNORE);
            }
            if (standardNode.getStatus() != NodeStatusEnum.IGNORE) {
                if (node.getStatus() == NodeStatusEnum.IGNORE) {
                    node.setStatus(NodeStatusEnum.PROCESSED);
                }
            }
        });

        return notifyNodeList;
    }

    public Node rollback(RollbackDTO dto) {
        boolean canNotRollback = nodes.stream().anyMatch(node -> node.nextNameList().size() > 1);
        if (canNotRollback) {
            throw new ServiceException("该流程不支持退回操作");
        }
        Node node = findNode(Long.valueOf(dto.getNodeId()));
        if (node == null) {
            log.error("node not found:{}", dto.getNodeId());
            return null;
        }
        if (node.getStatus() != NodeStatusEnum.ACTIVE) {
            return null;
        }
        if (node.getType() == NodeTypeEnum.START) {
            throw new ServiceException("都到开始节点了，你还想退回？想啥呢");
        }
        // 上一个节点
        if (dto.getRollbackType() == 1) {
            List<Node> notifyNode = node.preNameList().stream()
                .map(this::findNode)
                .peek(preNode -> preNode.setStatus(NodeStatusEnum.ACTIVE))
                .collect(Collectors.toList());
            node.setStatus(NodeStatusEnum.NOT_ACTIVE);
            return notifyNode.get(0);
        }
        // 发起节点
        if (dto.getRollbackType() == 0) {
            nodes.forEach(thisNode -> thisNode.setStatus(NodeStatusEnum.NOT_ACTIVE));
            this.startNode().setStatus(NodeStatusEnum.ACTIVE);
            return startNode();
        }
        return null;
//        // 上一个节点
//        if (dto.getRollbackType() == 0) {
//            boolean canNotRollback = node.preNameList().stream()
//                .map(this::findNode)
//                .anyMatch(preNode -> preNode.nextNameList().size() > 1);
//            if (canNotRollback) {
//                throw new ServiceException("当退回的节点的后置节点存在多个时，不可以回退");
//            }
//            node.preNameList().stream()
//                .map(this::findNode)
//                .forEach(preNode -> {
//                    if (preNode.getStatus() == NodeStatusEnum.PROCESSED) {
//                        preNode.setStatus(NodeStatusEnum.ACTIVE);
//                    }
//                });
//            node.setStatus(NodeStatusEnum.NOT_ACTIVE);
//            return;
//        }

    }

    public Node findCanDoAndCanExecuteNodeAny(Long userId, List<Long> postIdList) {
        return nodes.stream()
            .filter(node -> node.isUserCando(userId, postIdList))
            .findAny()
            .orElse(null);
    }
}
