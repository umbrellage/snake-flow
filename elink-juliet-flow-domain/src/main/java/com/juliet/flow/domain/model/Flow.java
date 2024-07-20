package com.juliet.flow.domain.model;

import com.alibaba.fastjson2.JSON;
import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.common.NotifyTypeEnum;
import com.juliet.flow.client.common.TodoNotifyEnum;
import com.juliet.flow.client.dto.NotifyDTO;
import com.juliet.flow.client.dto.RollbackDTO;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.client.common.FlowStatusEnum;
import com.juliet.flow.client.common.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.constant.FlowConstant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    /**
     * 最后操作的操作人
     */
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

    public void cleanParentId() {
        parentId = null;
    }

    /**
     * 拒绝
     */
    public void reject() {
        nodes.forEach(node -> {
            if (node.getStatus() != NodeStatusEnum.PROCESSED && node.getStatus() != NodeStatusEnum.IGNORE) {
                node.setStatus(NodeStatusEnum.IGNORE);
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
                .filter(node -> node.getStatus() == NodeStatusEnum.ACTIVE ||
                        node.getStatus() == NodeStatusEnum.TO_BE_CLAIMED ||
                        node.getStatus() == NodeStatusEnum.NOT_ACTIVE ||
                        node.getType() == NodeTypeEnum.END)
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


    /**
     * 判断用户是否是流程内的操作人
     * @param userId
     * @return
     */
    public boolean isFlowOperator(Long userId, List<Long> postIdList) {
        if (CollectionUtils.isEmpty(nodes)) {
            return false;
        }
        return nodes.stream()
            .anyMatch(node -> {
                return userId.equals(node.getProcessedBy()) ||
                    (!Collections.disjoint(postIdList, node.postIdLongList()) && !node.existOperator() && node.getStatus() == NodeStatusEnum.TO_BE_CLAIMED );
            });
    }

    public Node findTodoNodeAnyMatch(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        return nodes.stream()
                .filter(node -> userIdList.contains(node.getProcessedBy()) && node.isTodoNode())
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

    public Node endNode() {
        if (CollectionUtils.isNotEmpty(nodes)) {
            return nodes.stream()
                    .filter(node -> node.getType().equals(NodeTypeEnum.END))
                    .findAny()
                    .orElseThrow(() -> new ServiceException("找不到结束节点"));
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

    /**
     * 递归获取前置节点
     *
     * @param name
     * @return
     */
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
     * 通过节点状态获取节点
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
            currentNode.setClaimTime(LocalDateTime.now());
            currentNode.setProcessedTime(LocalDateTime.now());
        } else {
            currentNode.setProcessedBy(userId);
            currentNode.setProcessedTime(LocalDateTime.now());
            currentNode.setClaimTime(LocalDateTime.now());
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
        flow.setTemplateCode(templateCode);
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
                node.setFinishTime(null);
                node.setClaimTime(null);
                node.setActiveTime(null);
                toBeProcessedNodeList.add(node);
            }
        });
        for (Node node : toBeProcessedNodeList) {
            modifyNodeStatus(node);
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
//                .filter(Node::needCallbackMsg)
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

    /**
     * 流程自检，当发起了一条异常流程时，可能会改变原有的路线，所以我们在新发起的异常流程中进行了流程同步，但是由于被同步的流程是不进行
     *
     * @param param
     */
    public void flowSelfCheck(Map<String, Object> param) {
        nodes.stream()
                .filter(node -> node.getStatus() == NodeStatusEnum.NOT_ACTIVE)
                .forEach(node -> activeNode(node, param));
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

    public void activeNode(Node node, Map<String, Object> param) {
        log.info("node:{}", JSON.toJSON(node));
        boolean preHandled = ifPreNodeIsHandle(node.getName()) && (node.getActiveRule() == null || node.getActiveRule()
                .activeSelf(this));
        // 如果需要激活的节点的前置节点都已经完成，节点才可以激活
        if (preHandled) {
            if (node.getAccessRule() != null) {
                param.put(FlowConstant.INNER_FLOW, this);
                param.put(FlowConstant.CURRENT_NODE, node);
                boolean flag = node.getAccessRule().accessRule(param, node.getId());
                // 如果规则不匹配，递归修改后面节点的状态为忽略
                if (!flag) {
                    ignoreEqualAfterNode(node);
                }
            }
            if (node.getType() == NodeTypeEnum.END) {
                node.setStatus(NodeStatusEnum.PROCESSED);
                node.setProcessedTime(LocalDateTime.now());
                node.setFinishTime(LocalDateTime.now());
                node.setActiveTime(LocalDateTime.now());
                return;
            }
            if (node.getStatus() == NodeStatusEnum.NOT_ACTIVE) {
                // 规则分配
                node.regularDistribution(param, this);
                // 分配给流程内部节点的操作人
                node.regularFlowInnerOperator(this);
                if (node.nodeTodo()) {
                    node.setStatus(NodeStatusEnum.ACTIVE);
                    node.setActiveTime(LocalDateTime.now());
                    // 为什么已经有操作人了，还会存在认领时间为空呢，因为在新建异常流程情况下，会把一些在主流程中已处理的，
                    // 在异常流程中并未执行到的节点的激活时间，认领时间，完成时间都设置为空
                    if (node.getClaimTime() == null) {
                        node.setClaimTime(LocalDateTime.now());
                    }
                } else {
                    node.setStatus(NodeStatusEnum.TO_BE_CLAIMED);
                    node.setActiveTime(LocalDateTime.now());
                    node.setClaimTime(null);
                }
                return;
            }
            if (node.getStatus() == NodeStatusEnum.PROCESSED) {
                node.setStatus(NodeStatusEnum.ACTIVE);
                node.setActiveTime(LocalDateTime.now());
                node.setClaimTime(LocalDateTime.now());
                node.setFinishTime(null);
            }
        }
    }

    /**
     * 完成某一节点的处理，并且修改下一节点的状态从待激活到待认领，如果处理人不为null则修改为已认领
     *
     * @param nodeId
     */
    public void modifyNextNodeStatus(Long nodeId, Long userId, Map<String, Object> param) {
        Node currentNode = findNode(nodeId);
        if (currentNode == null) {
            return;
        }

        List<String> nextNameList = currentNode.nextNameList();
        nodes.forEach(node -> {
            if (node.getId().equals(nodeId)) {
                node.setStatus(NodeStatusEnum.PROCESSED);
                node.setUpdateTime(new Date());
                node.setProcessedTime(LocalDateTime.now());
                node.setProcessedBy(userId);
                node.setFinishTime(LocalDateTime.now());
            }
        });
        // 修改节点消息通知状态
        modifyNodeTodoStatusAndActiveSelf(nodeId, param);
        nextNameList.stream()
                .map(this::findNode)
                .forEach(node -> activeNode(node, param));
    }

    public void modifyNodeTodoStatusAndActiveSelf(Long completeNodeId, Map<String, Object> param) {
        nodes.stream()
                .filter(node -> node.getActiveRule() != null)
                .forEach(node -> {
                    List<Long> nodeIdList = node.getActiveRule().notifyNodeIds(this, param);
                    nodeIdList.forEach(nodeId -> {
                        Node activeNode = findNode(nodeId);
                        if (activeNode == null) {
                            throw new ServiceException("node active rule configuration error, nodeId:" + nodeId);
                        }
                        activeNode.setTodoNotify(TodoNotifyEnum.NOTIFY);
                    });
                    if (completeNodeId.equals(node.getId())) {
                        return;
                    }
                    boolean active = node.getActiveRule().activeSelf(this);
                    if (active && node.getStatus() == NodeStatusEnum.NOT_ACTIVE) {
                        activeNode(node, param);
                    }
                });
    }

    /**
     * 修改消息通知状态
     *
     * @param param
     */
    public void triggerTodo(Map<String, Object> param) {
        nodes.stream()
                .filter(node -> node.getActiveRule() != null)
                .forEach(node -> {
                    List<Long> nodeIdList = node.getActiveRule().notifyNodeIds(this, param);
                    nodeIdList.forEach(nodeId -> {
                        Node activeNode = findNode(nodeId);
                        if (activeNode == null) {
                            throw new ServiceException("node active rule configuration error, nodeId:" + nodeId);
                        }
                        activeNode.setTodoNotify(TodoNotifyEnum.NOTIFY);
                    });
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
                    node.setProcessedTime(LocalDateTime.now());
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
                    node.setProcessedTime(LocalDateTime.now());
                }
                if (node.getStatus() == NodeStatusEnum.ACTIVE) {
                    node.setProcessedBy(standardNode.getProcessedBy());
                    node.setProcessedTime(LocalDateTime.now());
                    node.setBindSuppliers(standardNode.getBindSuppliers());
                }
            }
        });

        return notifyNodeList;
    }


    /**
     * 从某个节点回退
     * @param node
     * @return
     */
    public List<Node> rollback(Node node) {
        List<Node> rollbackNodeList = new ArrayList<>();
        if (node == null) {
            throw new ServiceException("找不到节点信息");
        }
        if (node.getType() == NodeTypeEnum.START) {
            throw new ServiceException("开始节点无法退回");
        }
        List<String> preNameList = node.preNameList();
        preNameList.forEach(preName -> {
            Node preNode = findNode(preName);
            rollbackNodeList.add(preNode);
            rollbackToNode(preNode);
        });
        return rollbackNodeList;
    }

    /**
     * 回退到某个节点
     *
     * @param node
     */
    public void rollbackToNode(Node node) {
        if (node.getStatus() == NodeStatusEnum.PROCESSED) {
            node.setStatus(NodeStatusEnum.ACTIVE);
        }
        List<String> nextNameList = node.nextNameList();
        nextNameList.forEach(nextName -> {
            Node nextNode = findNode(nextName);
            recursionRollbackNode(nextNode);
        });

    }


    /**
     * 回退后递归修改流程状态
     *
     * @param node
     */
    public void recursionRollbackNode(Node node) {
        if (node.getStatus() == NodeStatusEnum.PROCESSED ||
                node.getStatus() == NodeStatusEnum.ACTIVE ||
                node.getStatus() == NodeStatusEnum.TO_BE_CLAIMED) {
            node.setStatus(NodeStatusEnum.NOT_ACTIVE);
            node.setActiveTime(null);
            node.setFinishTime(null);
            node.setClaimTime(null);
        }
        List<String> nextNameList = node.nextNameList();
        nextNameList.forEach(nextName -> {
            Node nextNode = findNode(nextName);
            recursionRollbackNode(nextNode);
        });
    }


    public Node rollback(RollbackDTO dto) {
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
        if (dto.getType() == 1) {
            List<Node> notifyNode = node.preNameList().stream()
                    .map(this::findNode)
                    .peek(preNode -> preNode.setStatus(NodeStatusEnum.ACTIVE))
                    .collect(Collectors.toList());
            node.setStatus(NodeStatusEnum.NOT_ACTIVE);
            node.setProcessedBy(null);
            node.setProcessedTime(null);
            return notifyNode.get(0);
        }
        // 发起节点
        if (dto.getType() == 0) {
            nodes.forEach(thisNode -> {
                if (thisNode.getType() != NodeTypeEnum.START) {
                    thisNode.setStatus(NodeStatusEnum.NOT_ACTIVE);
                    thisNode.setProcessedBy(null);
                    thisNode.setProcessedTime(null);
                }
            });
            this.startNode().setStatus(NodeStatusEnum.ACTIVE);
            return startNode();
        }
        return null;

    }

    public Node findCanDoAndCanExecuteNodeAny(Long userId, List<Long> postIdList, Long supplierId) {
        return nodes.stream()
                .filter(node -> node.isUserCando(userId, postIdList, supplierId))
                .findAny()
                .orElse(null);
    }

    /**
     * 提前结束流程
     */
    public void earlyEndFlow() {
        nodes.forEach(node -> {
            if (node.getStatus() != NodeStatusEnum.PROCESSED && node.getStatus() != NodeStatusEnum.IGNORE) {
                node.setStatus(NodeStatusEnum.PROCESSED);
            }
        });
        setStatus(FlowStatusEnum.INVALID);
    }


    public List<Node> canFlowAutomate(Map<String, Object> automateParam) {
        return getNodes().stream()
            .filter(e -> e.getStatus() == NodeStatusEnum.ACTIVE || e.getStatus() == NodeStatusEnum.TO_BE_CLAIMED)
            .filter(e -> StringUtils.isNotBlank(e.getFlowAutomateRuleName()))
            .filter(e -> e.getFlowAutomateRule() != null)
            .filter(e -> e.getFlowAutomateRule().flowAutomateForward(e,  automateParam))
            .collect(Collectors.toList());
    }

    public List<Node> canFlowRollback(Map<String, Object> automateParam) {
        return getNodes().stream()
            .filter(e -> e.getStatus() == NodeStatusEnum.ACTIVE || e.getStatus() == NodeStatusEnum.TO_BE_CLAIMED)
            .filter(e -> e.getFlowAutomateRule() != null)
            .filter(e -> e.getFlowAutomateRule().flowAutomateRollback(e,  automateParam))
            .collect(Collectors.toList());
    }

    public Node findNodeBySupplierId(Long supplierId) {
        return nodes.stream()
                .filter(e -> e.getStatus() == NodeStatusEnum.TO_BE_CLAIMED)
                .filter(e -> CollectionUtils.isNotEmpty(e.getBindSuppliers()))
                .filter(e -> e.getBindSuppliers().stream()
                        .map(Supplier::getSupplierId)
                        .anyMatch(k -> Objects.equals(k, supplierId)))
                .findAny()
                .orElse(null);


    }
}
