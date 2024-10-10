package com.juliet.flow.service.impl;

import com.alibaba.fastjson2.JSON;
import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.callback.MsgNotifyCallback;
import com.juliet.flow.client.common.TodoNotifyEnum;
import com.juliet.flow.client.common.thread.ThreadPoolFactory;
import com.juliet.flow.client.dto.*;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.client.common.FlowStatusEnum;
import com.juliet.flow.client.common.NodeStatusEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.common.utils.IdGenerator;
import com.juliet.flow.common.utils.JulietSqlUtil;
import com.juliet.flow.dao.SupplierDao;
import com.juliet.flow.domain.dto.TaskForwardDTO;
import com.juliet.flow.domain.entity.SupplierEntity;
import com.juliet.flow.domain.model.*;
import com.juliet.flow.domain.query.AssembleFlowCondition;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.repository.HistoryRepository;
import com.juliet.flow.service.FlowExecuteService;
import com.juliet.flow.service.TaskService;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;


/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Service
@Slf4j
public class FlowExecuteServiceImpl implements FlowExecuteService, TaskService {

    /**
     * 流程流转锁容器
     */
    private final Map<Long, Object> lockMap = new ConcurrentHashMap<>();

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private List<MsgNotifyCallback> msgNotifyCallbacks;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private SupplierDao supplierDao;

    @Override
    public NodeVO queryStartNodeById(FlowOpenDTO dto) {
        FlowTemplate template = null;
        if (dto.getTemplateId() != null) {
            template = flowRepository.queryTemplateById(dto.getTemplateId());
        }
        if (StringUtils.isNotBlank(dto.getCode())) {
            template = flowRepository.queryTemplateByCode(dto.getCode(), dto.getTenantId());
        }

        if (template == null) {
            return null;
        }
        Node node = template.getNodes().stream()
            .filter(nodeT -> StringUtils.isBlank(nodeT.getPreName()))
            .findAny()
            .orElseThrow(() -> new ServiceException("找不到开始节点"));
        // 如果是供应商不做判断
        if (dto.getSupplierOperator() != null && dto.getSupplierOperator()) {
            NodeVO nodeVO = node.toNodeVo(null);
            nodeVO.setCode(template.getCode());
            return nodeVO;
        }
        if (node.postAuthority(dto.getPostIdList())) {
            NodeVO nodeVO = node.toNodeVo(null);
            nodeVO.setCode(template.getCode());
            return nodeVO;
        }
        throw new ServiceException("该用户没有该节点的处理权限");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long startFlow(BpmDTO dto) {
        FlowTemplate flowTemplate = flowRepository.queryTemplateByCode(dto.getTemplateCode(), dto.getTenantId());
        if (flowTemplate == null) {
            throw new ServiceException("流程模版不存在");
        }
        Flow flow = flowTemplate.toFlowInstance(dto.getUserId());

        Long flowId = flowRepository.add(flow);
        Flow dbFlow = flowRepository.queryById(flowId);
        Node node = dbFlow.startNode();
        dbFlow.modifyNextNodeStatus(node.getId(), dto.getUserId(), dto.getData());
        log.info("init flow:{}", JSON.toJSONString(dbFlow));
        if (dbFlow.end()) {
            dbFlow.setStatus(FlowStatusEnum.END);
        }
        flowRepository.update(dbFlow);
        List<History> forwardHistory = dbFlow.forwardHistory(node.getId(), dto.getUserId());
        historyRepository.add(forwardHistory);

        // 流程流转完执行自动流转功能
        flowAutomate(flowId, dto.getData());
        callback(dbFlow.normalNotifyList());
        return flow.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public HistoricTaskInstance startFlowV2(BpmDTO dto) {
        FlowTemplate flowTemplate = flowRepository.queryTemplateByCode(dto.getTemplateCode(), dto.getTenantId());
        if (flowTemplate == null) {
            throw new ServiceException("流程模版不存在");
        }
        Flow flow = flowTemplate.toFlowInstance(dto.getUserId());

        Long flowId = flowRepository.add(flow);
        Flow dbFlow = flowRepository.queryById(flowId);
        Node node = dbFlow.startNode();
        dbFlow.modifyNextNodeStatus(node.getId(), dto.getUserId(), dto.getData());
        log.info("init flow:{}", JSON.toJSONString(dbFlow));
        if (dbFlow.end()) {
            dbFlow.setStatus(FlowStatusEnum.END);
        }
        flowRepository.update(dbFlow);
        List<History> forwardHistory = dbFlow.forwardHistory(node.getId(), dto.getUserId());
        historyRepository.add(forwardHistory);

        // 流程流转完执行自动流转功能
        flowAutomate(flowId, dto.getData());
        callback(dbFlow.normalNotifyList());

        return forwardHistory.stream()
                .map(History::toHistoricTask)
                .findAny()
                .orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long startOnlyFlow(BpmDTO dto) {
        FlowTemplate flowTemplate = flowRepository.queryTemplateByCode(dto.getTemplateCode(), dto.getTenantId());
        if (flowTemplate == null) {
            throw new ServiceException("流程模版不存在");
        }
        Flow flow = flowTemplate.toFlowInstance(dto.getUserId());
        flow.validate();
        flowRepository.add(flow);
        Flow dbFlow = flowRepository.queryById(flow.getId());
        callback(dbFlow.normalNotifyList());
        return flow.getId();
    }

    @Override
    public boolean flowEnd(Long flowId) {
        Flow flow = flowRepository.queryById(flowId);
        if (flow == null) {
            throw new ServiceException("流程不存在");
        }
        return flow.isFlowEnd();
    }

    @Override
    public NodeVO fieldNode(NodeFieldDTO dto) {
        Flow flow = flowRepository.queryById(dto.getFlowId());
        Node node = flow.findNode(dto.getFieldCodeList());
        return node.toNodeVo(flow);
    }

    public NodeVO nodeNew(TaskDTO dto) {
        Flow flow = flowRepository.queryById(dto.getFlowId());
        List<Flow> subFlowList = findSubFlowList(dto.getFlowId());
        // 如果节点id存在那么优先级肯定是最高的
        if (dto.getNodeId() != null) {
            Node node = flow.findNode(dto.getNodeId());
            if (node != null) {
                return node.toNodeVo(flow);
            }
            Flow sub = subFlowList.stream()
                .filter(subFlow -> {
                    return subFlow.getNodes().stream()
                        .anyMatch(subNode -> Objects.equals(subNode.getId(), dto.getNodeId()));
                })
                .findFirst()
                .orElseThrow(() -> new ServiceException("流程id与节点id不符合"));
            return sub.findNode(dto.getNodeId()).toNodeVo(sub);
        }

        if (dto.getUserId() != null) {

        }

        // TODO: 2024/9/4


        return null;
    }


    @Deprecated
    @Override
    public NodeVO node(TaskDTO dto) {
        // TODO: 2024/4/23 重写
        Flow flow = flowRepository.queryById(dto.getFlowId());
        if (flow == null) {
            return null;
        }
        List<Flow> subFlowList = findSubFlowList(dto.getFlowId());
        Optional.ofNullable(dto.getUserId()).orElseThrow(() -> new ServiceException("用户id 不可以为空"));
        Node node;
        if (dto.getNodeId() != null) {
            node = flow.findNode(dto.getNodeId());
            if (node != null) {
                return node.toNodeVo(flow);
            }
            Flow sub = subFlowList.stream()
                .filter(subFlow -> {
                    return subFlow.getNodes().stream()
                        .anyMatch(subNode -> Objects.equals(subNode.getId(), dto.getNodeId()));
                })
                .findFirst()
                .orElseThrow(() -> new ServiceException("流程id与节点id不符合"));
            return sub.findNode(dto.getNodeId()).toNodeVo(sub);
        } else {
            node = flow.findTodoNode(dto.getUserId());
        }
        if (node == null && dto.getSupplierId() != null) {
            node = flow.findNodeBySupplierId(dto.getSupplierId());
        }
        if (node == null) {
            node = flow.findCanDoAndCanExecuteNodeAny(dto.getUserId(), dto.getPostIdList(), dto.getSupplierId());
        }
        if (node != null) {
            return node.toNodeVo(flow);
        }
        node = findSubFlowList(flow.getId()).stream()
                .map(subFlow -> subFlow.findTodoNode(dto.getUserId()))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
        if (node == null) {
            Node canDoNode = flow.findCanDoAndCanExecuteNodeAny(dto.getUserId(), dto.getPostIdList(),
                    dto.getSupplierId());
            if (canDoNode != null) {
                return canDoNode.toNodeVo(flow);
            }
            Node subCandoNode = subFlowList.stream()
                    .map(subFlow -> subFlow.findCanDoAndCanExecuteNodeAny(dto.getUserId(), dto.getPostIdList(), dto.getSupplierId()))
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse(null);
            if (subCandoNode != null) {
                return subCandoNode.toNodeVo(flow);
            }
            return null;
        }
        return node.toNodeVo(flow);

        //  不做岗位的校验了
//        if (CollectionUtils.isEmpty(dto.getPostIdList())) {
//            return node.toNodeVo(null);
//        }
//        return node.postAuthority(dto.getPostIdList()) ? node.toNodeVo(null) : null;
    }

    @Override
    public List<FlowVO> flowList(FlowIdListDTO dto) {
        StopWatch sw = new StopWatch();
        if (CollectionUtils.isEmpty(dto.getFlowIdList())) {
            log.error("流程ID列表为空");
            return Collections.emptyList();
        }
        sw.start("flowRepository.queryByIdList");
        AssembleFlowCondition condition = new AssembleFlowCondition();
        condition.setExcludeFields(dto.getExcludeFields());
        List<Flow> mainFlowList = flowRepository.queryByIdList(dto.getFlowIdList(), condition);
        sw.stop();
        sw.start("flowRepository.listFlowByParentId");
        List<Long> flowIdList = mainFlowList.stream().map(Flow::getId).collect(Collectors.toList());
        Map<Long, List<FlowVO>> subFlowMap = flowRepository.listFlowByParentId(flowIdList, condition)
                .stream().map(flow -> flow.flowVO(Collections.emptyList()))
                .collect(Collectors.groupingBy(FlowVO::getParentId));
        sw.stop();
        log.info("flowList_perf:{}", sw.prettyPrint());
        return mainFlowList.stream()
                .map(flow -> flow.flowVO(subFlowMap.get(flow.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public NodeVO findNodeByFlowIdAndNodeId(TaskDTO dto) {
        Flow flow = flowRepository.queryById(dto.getFlowId());
        Optional.ofNullable(flow).orElseThrow(() -> new ServiceException("找不到流程"));
        List<Flow> subFlowList = flowRepository.listFlowByParentId(flow.getId());
        Node node;
        node = flow.findNode(dto.getNodeId());
        if (node == null) {
            node = subFlowList.stream()
                    .map(e -> e.findNode(dto.getNodeId()))
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElseThrow(() -> new ServiceException("当前用户没有操作权限"));
        }
        return node.toNodeVo(flow);
    }

    @Override
    public List<String> customerStatus(String code, Long tenantId) {
        FlowTemplate flowTemplate = flowRepository.queryTemplateByCode(code, tenantId);
        if (flowTemplate == null) {
            return Collections.emptyList();
        }
        return flowTemplate.getNodes().stream()
                .map(Node::getCustomStatus)
                .filter(StringUtils::isNotBlank).distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<HistoricTaskInstance> execute(TaskExecute dto) {
        switch (dto.getTaskType()) {
            case ROLLBACK:
                rollback(dto);
                return Collections.emptyList();
            case REJECT:
                reject(dto);
                return Collections.emptyList();
            case REDO:
                return redo(dto);
            default:
                return Collections.emptyList();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void invalid(InvalidDTO dto) {
        Flow flow = JulietSqlUtil.findById(Long.valueOf(dto.getFlowId()), flowRepository::queryById, "flow not found");
        flow.setStatus(FlowStatusEnum.INVALID);
        List<Flow> flowList = flowRepository.queryMainFlowById(Collections.singletonList(flow.getId()));
        flowList.forEach(subFlow -> subFlow.setStatus(FlowStatusEnum.INVALID));
        flowList.forEach(flowRepository::update);
        flowRepository.update(flow);
        NotifyDTO notifyDTO = flow.invalidFlow();
        callback(Collections.singletonList(notifyDTO));
    }

    @Override
    public void delete(InvalidDTO dto) {
        flowRepository.deleteFlow(Long.valueOf(dto.getFlowId()));
    }

    @Override
    public void triggerTodo(Long flowId, Map<String, Object> triggerParam) {
        Flow flow = flowRepository.queryById(flowId);
        Optional.ofNullable(flow).ifPresent(data -> {
            data.triggerTodo(triggerParam);
            flowRepository.update(data);
            callback(data.normalNotifyList());
        });
    }

    @Override
    public void triggerTodo(Long flowId, Long nodeId) {
        if (flowId == null || nodeId == null) {
            return;
        }
        Flow flow = JulietSqlUtil.findById(flowId, flowRepository::queryById, "找不到流程");
        flow.getNodes()
            .forEach(node -> {
                if (Objects.equals(node.getId(), nodeId)) {
                    node.setTodoNotify(TodoNotifyEnum.NOTIFY);
                }
            });
        flowRepository.update(flow);
        callback(flow.normalNotifyList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void earlyEndFlow(Long flowId) {
        Flow flow = flowRepository.queryById(flowId);
        if (flow == null) {
            throw new ServiceException("流程要不要再检查下呢");
        }
        List<Flow> flowList = flowRepository.listFlowByParentId(flowId);
        flowList.add(flow);
        flowList.stream()
                .peek(Flow::earlyEndFlow)
                .forEach(e -> flowRepository.update(e));
        callback(flow.normalNotifyList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void flowAutomate(Long flowId, Map<String, Object> automateParam) {
        Object lock = lockMap.computeIfAbsent(flowId, id -> new Object());
        try {
            synchronized (lock) {
                Flow flow = flowRepository.queryById(flowId);
                flowAutomate(flow, automateParam);
            }
        } finally {
            lockMap.remove(flowId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void flowAutomate(Flow flow, Map<String, Object> automateParam) {
        do {
            List<Node> flowAutomateNodeList = flow.canFlowAutomate(automateParam);
            List<Long> nodeIdList = flowAutomateNodeList.stream()
                .map(Node::getId)
                .collect(Collectors.toList());
            flow.getNodes()
                .stream()
                .filter(e -> nodeIdList.contains(e.getId()))
                .forEach(e -> e.setStatus(NodeStatusEnum.ACTIVE));
            flowRepository.update(flow);
            Flow finalFlow = flow;
            flowAutomateNodeList.forEach(node -> {
                NodeFieldDTO fieldDTO = new NodeFieldDTO();
                fieldDTO.setFlowId(finalFlow.getId());
                fieldDTO.setNodeId(node.getId());
                fieldDTO.setData(automateParam);
                forward(fieldDTO);
            });
            flow = flowRepository.queryById(flow.getId());
        } while (CollectionUtils.isNotEmpty(flow.canFlowAutomate(automateParam)));

        List<Node> rollbackNodeList = flow.canFlowRollback(automateParam);

        for (Node rollbackNode : rollbackNodeList) {
            flow.rollback(rollbackNode);
        }

        flowRepository.update(flow);
        // TODO: 2024/8/27 这里真的需要吗？ 主流程节点在流转时会一并将异常流程的节点流转掉, 先注释掉吧
//        List<Flow> flowList = flowRepository.listFlowByParentId(flow.getId());
//        if (CollectionUtils.isNotEmpty(flowList)) {
//            flowList.forEach(subFlow -> flowAutomate(subFlow, automateParam));
//        }

    }

    public Flow tryFowAutomate(Flow flow, Map<String, Object> automateParam) {
        do {
            List<Node> flowAutomateNodeList = flow.canFlowAutomate(automateParam);
            List<Long> nodeIdList = flowAutomateNodeList.stream()
                .map(Node::getId)
                .collect(Collectors.toList());
            flow.getNodes()
                .stream()
                .filter(e -> nodeIdList.contains(e.getId()))
                .forEach(e -> e.setStatus(NodeStatusEnum.ACTIVE));
            for (Node node : flowAutomateNodeList) {
                flow = tryForwardFlowTask(flow, node, null, automateParam);
            }
        } while (CollectionUtils.isNotEmpty(flow.canFlowAutomate(automateParam)));

        List<Node> rollbackNodeList = flow.canFlowRollback(automateParam);
        for (Node rollbackNode : rollbackNodeList) {
            flow.rollback(rollbackNode);
        }


        return flow;
    }

    @Override
    public void recoverFlow(FlowIdDTO flowId) {
        if (flowId.getFlowId() == null) {
            return;
        }
        Flow flow = JulietSqlUtil.findById(flowId.getFlowId(), flowRepository::queryById, "not found flow");
        if (flow.getStatus() != FlowStatusEnum.INVALID) {
            throw new ServiceException("流程状态有误");
        }
        flow.setStatus(FlowStatusEnum.IN_PROGRESS);
        flowRepository.update(flow);
        callback(flow.normalNotifyList());
    }

    @Override
    public void endFlowRollback(FlowIdDTO flowId, Integer level) {
        Flow flow = JulietSqlUtil.findById(flowId.getFlowId(), flowRepository::queryById, "not found flow");
        Node endNode = flow.endNode();
        List<Node> nodeList = new ArrayList<>();
        nodeList.add(endNode);
        for (int i = 0; i < level; i++) {
            nodeList.forEach(node -> node.setStatus(NodeStatusEnum.NOT_ACTIVE));
            nodeList = nodeList.stream()
                    .map(Node::preNameList)
                    .flatMap(Collection::stream)
                    .distinct()
                    .map(flow::findNode)
                    .collect(Collectors.toList());

        }

        List<Long> rollbackNodeIdList = nodeList.stream()
                .map(Node::getId)
                .collect(Collectors.toList());

        flow.getNodes()
                .stream()
                .filter(node -> rollbackNodeIdList.contains(node.getId()))
                .forEach(node -> {
                    if (node.getProcessedBy() == null || node.getProcessedBy() == 0) {
                        node.setStatus(NodeStatusEnum.TO_BE_CLAIMED);
                    } else {
                        node.setStatus(NodeStatusEnum.ACTIVE);
                    }
                });
        flow.setStatus(FlowStatusEnum.IN_PROGRESS);
        flowRepository.update(flow);
        callback(flow.normalNotifyList());
    }

    @Override
    public void designationOperator(DesignationOperator dto) {
        if (CollectionUtils.isEmpty(dto.getNodeIdList())) {
            return;
        }
        Flow flow = flowRepository.queryById(dto.getFlowId());
        if (flow == null) {
            return;
        }
        flow.getNodes().forEach(node -> {
            if (dto.getNodeIdList().contains(node.getId())) {
                node.setProcessedBy(dto.getOperator());
                node.setProcessedTime(LocalDateTime.now());
                if (node.getStatus() == NodeStatusEnum.TO_BE_CLAIMED) {
                    node.setStatus(NodeStatusEnum.ACTIVE);
                }
            }
        });
        flowRepository.update(flow);
    }

    @Override
    public void resetMsgByFlowId(Long flowId) {
        Flow flow = JulietSqlUtil.findById(flowId, flowRepository::queryById, "找不到流程，id:" + flowId);
        callback(flow.normalNotifyList());
    }


    @Transactional(rollbackFor = Exception.class)
    public List<HistoricTaskInstance> redo(TaskExecute dto) {
        RedoDTO redo = (RedoDTO) dto;
        Long redoNodeId = null;
        Flow flow = flowRepository.queryById(redo.getFlowId());
        if (flow == null) {
            throw new ServiceException("不存在该流程，亲你再检查下吧！");
        }
        // 重做后将老的流程以及异常流程都结束掉
        List<Flow> subFlowList = flowRepository.listFlowByParentId(redo.getFlowId());
        subFlowList.add(flow);
        subFlowList.stream()
                .peek(Flow::earlyEndFlowAndNode)
                .forEach(e -> flowRepository.update(e));

        if (redo.getNodeId() == null) {
            redoNodeId = flow.startNode().getId();
        }
        Node node = flow.findNodeThrow(redoNodeId);
        Flow newFlow = flow.subFlow();
        newFlow.cleanParentId();
        Node executeNode = newFlow.findNode(node.getName());
        newFlow.modifyNodeStatus(executeNode);
        newFlow.modifyNextNodeStatus(executeNode.getId(), redo.getUserId(), redo.getParam());
        Long newFlowId = flowRepository.add(newFlow);
        newFlow = flowRepository.queryById(newFlowId);

        List<History> historyList = newFlow.forwardHistory(executeNode.getId(), redo.getUserId());
        historyRepository.add(historyList);
        callback(newFlow.normalNotifyList());
        callback(Collections.singletonList(flow.flowEndNotify()));

        // 流程流转完执行自动流转功能
        flowAutomate(newFlow.getId(), redo.getParam());

        return historyList.stream()
                .map(History::toHistoricTask)
                .collect(Collectors.toList());
    }

    public void reject(TaskExecute dto) {
        RejectDTO reject = (RejectDTO) dto;
        Flow flow = flowRepository.queryById(Long.valueOf(reject.getFlowId()));
        if (flow == null) {
            throw new ServiceException("流程不存在");
        }
        List<NotifyDTO> notifyList = flow.getNodes().stream()
                .filter(node -> node.getStatus() == NodeStatusEnum.ACTIVE ||
                        node.getStatus() == NodeStatusEnum.TO_BE_CLAIMED)
                .map(node -> node.toNotifyComplete(flow))
                .collect(Collectors.toList());
        flow.reject();
        flowRepository.update(flow);
        History history = History.of(reject, null, flow);
        historyRepository.add(history);
        callback(notifyList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void rollback(TaskExecute dto) {
        RollbackDTO rollback = (RollbackDTO) dto;
        Flow flow = flowRepository.queryById(Long.valueOf(rollback.getFlowId()));
        if (flow == null) {
            throw new ServiceException("流程不存在，检查下流程id");
        }
        Node node = flow.rollback(rollback);
        flowRepository.update(flow);
        History history = History.of(rollback, node.getId(), flow);
        historyRepository.add(history);
        callback(flow.normalNotifyList());
    }


    private List<Flow> findSubFlowList(Long id) {
        if (id == null) {
            return Collections.emptyList();
        }
        List<Flow> flowList = flowRepository.listFlowByParentId(id);
        if (CollectionUtils.isEmpty(flowList)) {
            return Collections.emptyList();
        }
        return flowList;
    }


    @Override
    public List<NodeVO> currentNodeList(Long flowId) {
        Flow flow = flowRepository.queryById(flowId);
        if (flow == null) {
            return Collections.emptyList();
        }
        List<NodeStatusEnum> nodeStatusEnumList = Stream.of(NodeStatusEnum.TO_BE_CLAIMED, NodeStatusEnum.ACTIVE)
                .collect(Collectors.toList());
        List<Flow> flowList = flowRepository.listFlowByParentId(flowId);
        flowList.add(flow);

        // 子流程的节点都是当作主流程的待办来处理
        return flowList.stream()
                .map(subFlow ->
                        subFlow.getNodeByNodeStatus(nodeStatusEnumList)
                                .stream()
                                .map(node -> node.toNodeVo(subFlow))
                                .collect(Collectors.toList())
                )
                .flatMap(Collection::stream)
                .collect(collectingAndThen(toCollection(() ->
                        new TreeSet<>(Comparator.comparing(NodeVO::getName))), ArrayList::new));
    }

    /**
     * @param dto flowId 可能是主流程id，也可能是异常流程id，如果是异常流程id，找到主流程然后认领主流程下所有异常流程的节点
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimTask(TaskDTO dto) {
        Flow flow = flowRepository.queryById(dto.getFlowId());
        Optional.ofNullable(flow).orElseThrow(() -> new ServiceException("找不到流程"));
        List<Flow> subFlowList = flowRepository.listFlowByParentId(flow.getId());
        Node node;
        node = flow.findNode(dto.getNodeId());
        if (node == null) {
            node = subFlowList.stream()
                    .map(e -> e.findNode(dto.getNodeId()))
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElseThrow(() -> new ServiceException("找不到节点"));
        }
        BusinessAssert.assertTrue(node.ifLeaderAdjust(dto.getLocalUser()), StatusCode.SERVICE_ERROR,
                "当前操作人没有权限调整");
        node.setProcessedBy(dto.getUserId());
        node.setProcessedTime(LocalDateTime.now());
        if (flow.hasParentFlow()) {
            flow = flowRepository.queryById(flow.getParentId());
        }
        flow.claimNode(node.getName(), dto.getUserId());
        String nodeName = node.getName();
        subFlowList.forEach(subFlow -> {
            subFlow.claimNode(nodeName, dto.getUserId());
            flowRepository.update(subFlow);
        });
        flowRepository.update(flow);
        // 异步发送待办通知
        callback(Collections.singletonList(node.toNotifyNormal(flow)));
    }

    @Override
    public List<NodeVO> todoNodeList(UserDTO dto) {
        return todoNodeList(dto, TodoNotifyEnum.NOTIFY);
    }

    @Override
    public List<NodeVO> canDoNodeList(UserDTO dto) {
        return todoNodeList(dto, TodoNotifyEnum.NO_NOTIFY);
    }

    public List<NodeVO> todoNodeList(UserDTO dto, TodoNotifyEnum notify) {
        StopWatch watch = new StopWatch("待办或者可办代码执行时间监测");
        List<Node> userIdNodeList = new ArrayList<>();
        List<Node> supervisorIdNodeList = new ArrayList<>();
        List<Node> postIdNodeList = new ArrayList<>();
        List<Node> supplierNodeList = new ArrayList<>();
        Future<List<Node>> supplierNodeFuture = null;
        Future<List<Node>> postIdNodeListFuture = null;

        Future<List<Node>> userIdNodeFuture = ThreadPoolFactory.THREAD_POOL_TODO_MAIN
                .submit(() -> flowRepository.listNode(NodeQuery.findByUserId(dto.getUserId())));

        Future<List<Node>> supervisorIdNodeFuture = ThreadPoolFactory.THREAD_POOL_TODO_MAIN
                .submit(() -> flowRepository.listNode(NodeQuery.findBySupervisorId(dto.getUserId())));

        if (CollectionUtils.isNotEmpty(dto.getPostId())) {
            postIdNodeListFuture = ThreadPoolFactory.THREAD_POOL_TODO_MAIN
                    .submit(() -> flowRepository.listNode(NodeQuery.findByPostId(dto.getPostId())));
        }
        if (dto.getSupplier() != null) {
            supplierNodeFuture = ThreadPoolFactory.THREAD_POOL_TODO_MAIN
                    .submit(() -> flowRepository.listNode(dto.supplierId(), dto.supplierType()));
        }

        watch.start("查我的待办");
        userIdNodeList = ThreadPoolFactory.get(userIdNodeFuture);
        watch.stop();
        watch.start("查我的分配");
        supervisorIdNodeList = ThreadPoolFactory.get(supervisorIdNodeFuture);
        watch.stop();
        if (postIdNodeListFuture != null) {
            watch.start("查我的岗位可认领");
            postIdNodeList = ThreadPoolFactory.get(postIdNodeListFuture).stream()
                    .filter(node -> node.getProcessedBy() == null || node.getProcessedBy().longValue() == 0L)
                    .collect(Collectors.toList());
            watch.stop();
        }
        if (supplierNodeFuture != null) {
            watch.start("查供应商");
            supplierNodeList = ThreadPoolFactory.get(supplierNodeFuture).stream()
                    .filter(node -> node.getProcessedBy() == null || node.getProcessedBy() == 0L)
                    .collect(Collectors.toList());
            watch.stop();
        }

        List<Long> flowIdList = Stream.of(userIdNodeList, postIdNodeList, supervisorIdNodeList, supplierNodeList)
                .flatMap(Collection::stream)
                .map(Node::getFlowId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(flowIdList)) {
            return Collections.emptyList();
        }

        watch.start("流程查询");
        Map<Long, Flow> flowMap = flowRepository.queryByIdList(flowIdList, AssembleFlowCondition.builder()
                        .excludePost(false)
                        .excludeForm(false)
                        .excludeFields(false)
                        .build()).stream()
                .collect(Collectors.toMap(Flow::getId, Function.identity(), (v1, v2) -> v1));
        List<NodeVO> nodeVOList = Stream.of(userIdNodeList, postIdNodeList, supervisorIdNodeList, supplierNodeList)
                .flatMap(Collection::stream)
                .filter(node -> flowMap.get(node.getFlowId()) != null)
                .filter(node -> flowMap.get(node.getFlowId()).getStatus() != FlowStatusEnum.INVALID)
                .filter(node -> node.getTodoNotify() == notify)
                .map(node -> node.toNodeVo(flowMap.get(node.getFlowId())))
                .collect(Collectors.toList());

        List<NodeVO> result = nodeVOList.stream().collect(collectingAndThen(toCollection(() ->
                new TreeSet<>(Comparator.comparing(NodeVO::distinct))), ArrayList::new));
        watch.stop();
        log.info("data {}", watch.prettyPrint());
        return result;
    }

    @Override
    public FlowVO beforehandForward(NodeFieldDTO dto) {
        Flow flow = flowRepository.queryById(dto.getFlowId());
        if (flow == null) {
            return null;
        }
        List<Flow> subFlowList = flowRepository.listFlowByParentId(flow.getId());
        return tryTask(flow, subFlowList, dto.getNodeId(), dto.getUserId(), dto.getData(), dto.getSkipCreateSubFlow());
    }

    @Override
    public FlowVO beforehandInitBpmForward(BpmDTO dto) {
        FlowTemplate flowTemplate = flowRepository.queryTemplateByCode(dto.getTemplateCode(), dto.getTenantId());
        if (flowTemplate == null) {
            throw new ServiceException("流程模版不存在");
        }
        Flow flow = flowTemplate.toFlowInstance(dto.getUserId());
        Node node = flow.startNode();
        flow.modifyNextNodeStatus(node.getId(), dto.getUserId(), dto.getData());
        if (flow.end()) {
            flow.setStatus(FlowStatusEnum.END);
        }

        return tryFowAutomate(flow, dto.getData()).flowVO(Collections.emptyList());
    }

    @Override
    public List<FlowVO> flowListByOperator(String flowCode, Long userId, List<Long> postIdList) {
        List<Flow> mainFlowList = flowRepository.listFlow(flowCode, userId, postIdList);
        List<Long> flowIdList = mainFlowList.stream().map(Flow::getId).collect(Collectors.toList());
        Map<Long, List<FlowVO>> subFlowMap = flowRepository.listFlowByParentId(flowIdList)
            .stream().map(flow -> flow.flowVO(Collections.emptyList()))
            .collect(Collectors.groupingBy(FlowVO::getParentId));
        return mainFlowList.stream()
            .map(flow -> flow.flowVO(subFlowMap.get(flow.getId())))
            .collect(Collectors.toList());
    }


    @Override
    public void trxTest() {
        trxTest2();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public void trxTest2() {
        SupplierEntity supplier1 = new SupplierEntity();
        supplier1.setId(IdGenerator.getId());
        supplier1.setNodeId(11L);
        supplier1.setSupplierType("xx");
        supplier1.setSupplierId(12L);
        supplier1.setSupplierName("xxxx");
        supplier1.setDelFlag(1);
        supplier1.setCreateTime(new Date());
        supplier1.setUpdateTime(new Date());
        supplier1.setCreateBy(1L);
        supplier1.setUpdateBy(1L);
        supplier1.setTenantId(1L);

        supplierDao.insert(supplier1);

        throw new ServiceException("xxxx");

//        SupplierEntity supplier2 = new SupplierEntity();
//        supplier2.setId(IdGenerator.getId());
//        supplier2.setNodeId(11L);
//        supplier2.setSupplierType("aa");
//        supplier2.setSupplierId(13L);
//        supplier2.setSupplierName("aa");
//        supplier2.setDelFlag(1);
//        supplier2.setCreateTime(new Date());
//        supplier2.setUpdateTime(new Date());
//        supplier2.setCreateBy(1L);
//        supplier2.setUpdateBy(1L);
//        supplier2.setTenantId(1L);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void distributionNodeOperator4Post(Long flowId, Long postId, Long userId) {
        if (postId == null || userId == null || flowId == null) {
            return;
        }
        Flow flow = JulietSqlUtil.findById(flowId, flowRepository::queryById, "找不到流程");
        List<Flow> subFlowList = flowRepository.listFlowByParentId(flowId);
        flow.distributionNodeOperator4Post(postId, userId);

        if (CollectionUtils.isNotEmpty(subFlowList)) {
            subFlowList.forEach(subFlow -> subFlow.distributionNodeOperator4Post(postId, userId));
        }

        flowRepository.update(flow);
        subFlowList.forEach(subFlow -> flowRepository.update(subFlow));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<HistoricTaskInstance> forward(NodeFieldDTO dto) {
        Object lock = lockMap.computeIfAbsent(dto.getFlowId(), id -> new Object());
        synchronized (lock) {
            try {
                List<HistoricTaskInstance> historicTaskInstanceList = new ArrayList<>();
                // 需要被执行的节点列表
                List<Node> executableNode = new ArrayList<>();
                Flow flow = flowRepository.queryById(dto.getFlowId());
                if (flow == null) {
                    return historicTaskInstanceList;
                }
                Node currentFlowNode = flowRepository.queryNodeById(dto.getNodeId());
                // 异常子流程
                List<Flow> subFlowList = new ArrayList<>();
                // 主流程
                Flow mainFlow = null;
                // 如果当前需要处理的是异常流程的节点
                if (flow.hasParentFlow()) {
                    subFlowList = flowRepository.listFlowByParentId(flow.getParentId());
                    mainFlow = flowRepository.queryById(flow.getParentId());
                }
                if (!flow.hasParentFlow()) {
                    subFlowList = flowRepository.listFlowByParentId(flow.getId());
                    mainFlow = flow;
                }
                assert mainFlow != null;
//        if (mainFlow.isFlowEnd()) {
//            throw new ServiceException("流程已结束");
//        }
                subFlowList.add(mainFlow);
                if (CollectionUtils.isNotEmpty(subFlowList)) {
                    subFlowList.stream()
                        .filter(subFlow -> {
                            Node node = subFlow.findNode(currentFlowNode.getName());
                            return node.isNormalExecutable() && subFlow.ifPreNodeIsHandle(node.getName());
                        })
                        .forEach(subFlow -> executableNode.add(subFlow.findNode(currentFlowNode.getName())));
                }

                executableNode.add(currentFlowNode);

                List<Node> nodeList = new ArrayList<>(
                    executableNode.stream().collect(Collectors.toMap(Node::getId, Function.identity(), (v1, v2) -> v1))
                        .values());

                for (Node node : nodeList) {
                    List<HistoricTaskInstance> taskInstances = task(mainFlow, node.getId(), node.getProcessedBy(),
                        dto.getData(), dto.getSkipCreateSubFlow());
                    historicTaskInstanceList.addAll(taskInstances);
                }

                return historicTaskInstanceList.stream()
                    .distinct()
                    .collect(Collectors.toList());
            } finally {
                lockMap.remove(dto.getFlowId());
            }

        }
    }

    /**
     * <ul>
     * 该方法主要分4种情况
     * 待处理的节点主要分为两种节点:
     * 一. 主流程中的节点
     * 1. 当前要处理的节点为异常节点，且存在异常流程 ------>抛出异常 TODO: 2023/5/11 后面如果支持多条异常流程则把1的处理删除
     * 2. 当前要处理的节点为异常节点，（存在异常流程且异常流程都已结束）或者 （不存在异常流程） -------> 创建一条异常流程
     * 3. 当前要处理的节点为非异常节点 ---------> 正常处理节点的逻辑走
     * 二. 异常流程中的节点
     * 4. 当前节点为异常流程中的节点，且该异常流程并未结束 --------> 按照处理异常流程和正常流程的方式处理
     * </ul>
     *
     * @param mainFlow 主流程
     * @param nodeId
     * @param userId
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized List<HistoricTaskInstance> task(Flow mainFlow, Long nodeId, Long userId,
                                                        Map<String, Object> data, Boolean skipCreateSubFlow) {
        // 获取要处理的节点信息，该节点可能有两种情况 1. 他是主流程的节点，2. 他是异常子流程的节点
        Node node = mainFlow.findNode(nodeId);
        // 如果是主流程的节点
        if (node != null) {
            if (!node.isExecutable()) {
                return Collections.emptyList();
            }
            // 当节点是异常节点时
            if (node.isProcessed()) {
                // 如果不需要创建异常流程那么就直接返回
                if (skipCreateSubFlow != null && skipCreateSubFlow) {
                    return Lists.newArrayList();
                }
                TaskForwardDTO forwardDTO = TaskForwardDTO.valueOf(mainFlow, node, userId, data);
                return createSubFlowTask(forwardDTO);
            }
            // 当节点是非异常节点时, 因为是主流程的节点，主流程不关心是否需要合并异常流程，这个操作让异常流程去做，因为异常流程在创建是肯定比主流程慢
            // 主流程只需要判断下是否存在异常流程为结束，如果存在，主流程在完成整个流程前等待异常流程合并至主流程
            if (!node.isProcessed()) {
                TaskForwardDTO forwardDTO = TaskForwardDTO.valueOf(mainFlow, node, userId, data);
                return forwardMainFlowTask(forwardDTO);
            }
        }
        if (node == null) {
            node = new Node();
            node.setId(nodeId);
            TaskForwardDTO forwardDTO = TaskForwardDTO.valueOf(mainFlow, node, userId, data);
            return forwardSubFlowTask(forwardDTO);
        }
        return Collections.emptyList();
    }

    public FlowVO tryTask(Flow mainFlow, List<Flow> subFlowList, Long nodeId, Long userId, Map<String, Object> data, Boolean skipCreateSubFlow) {
        try {
            Node mainNode = mainFlow.findNode(nodeId);
            if (mainNode != null) {
                // 主流程节点，但不可以操作，返回null
                if (!mainNode.isExecutable()) {
                    return mainFlow.flowVONew(subFlowList);
                }
                // 主流程节点，是正常流转的节点
                if (!mainNode.isProcessed()) {
                    Flow result =  tryForwardFlowTask(mainFlow, mainNode, userId, data);
                    // 因为要返回整条流程所以加了上面的代码
                    // -------------------------------------------
                    tryFowAutomate(result, data);
                    if (CollectionUtils.isNotEmpty(subFlowList)) {
                        subFlowList.forEach(subFlow -> {
                            Node subNode = subFlow.findNode(mainNode.getName());
                            if (subNode.getStatus() == NodeStatusEnum.ACTIVE) {
                                tryForwardFlowTask(subFlow, subNode, userId, data);
                                tryFowAutomate(subFlow, data);
                            }
                        });

                        return mainFlow.flowVONew(subFlowList);
                    }

                    return mainFlow.flowVO(Collections.emptyList());
                    // ------------------------------------------------
//                    return tryFowAutomate(result, data).flowVO(Collections.emptyList());
                }
                // 主流程节点，且是异常节点，但是不需要创建异常流程
                if (mainNode.isProcessed() && skipCreateSubFlow) {
                    return mainFlow.flowVONew(subFlowList);
                }
                // 主流程节点，且是异常节点，但是需要创建异常流程
                if (mainNode.isProcessed() && !skipCreateSubFlow) {
                    Flow subNewFlow = mainFlow.subFlow();
                    subNewFlow.modifyNodeStatus(mainNode);
                    Node subNode = subNewFlow.findNode(mainNode.getName());
                    tryForwardFlowTask(subNewFlow, subNode, userId, data);
                    // ------------------
                    tryFowAutomate(subNewFlow, data);
                    List<Flow> calibrateFlowList = new ArrayList<>();
                    calibrateFlowList.add(mainFlow);
                    if (CollectionUtils.isNotEmpty(subFlowList)) {
                        calibrateFlowList.addAll(subFlowList);
                    }
                    syncFlow(calibrateFlowList, subNewFlow);
                    calibrateFlowList.forEach(calibrateFlow -> calibrateFlow.flowSelfCheck(data));
                    if (CollectionUtils.isNotEmpty(subFlowList)) {
                        subFlowList.forEach(subFlow -> {
                            Node node = subFlow.findNode(mainNode.getName());
                            if (node.getStatus() == NodeStatusEnum.ACTIVE) {
                                tryForwardFlowTask(subFlow, node, userId, data);
                                tryFowAutomate(subFlow, data);
                            }
                        });

                        subFlowList.add(subNewFlow);
                        return mainFlow.flowVONew(subFlowList);
                    }

                    return mainFlow.flowVONew(Collections.singletonList(subNewFlow));
                    // ------------------
//                    return tryFowAutomate(result, data).flowVO(Collections.emptyList());
                }
            }
            // 如果是子流程的节点
            if (CollectionUtils.isNotEmpty(subFlowList)) {
//                Flow flow = subFlowList.stream()
//                    .filter(subFlow -> subFlow.findNode(nodeId) != null)
//                    .findAny()
//                    .orElse(null);
//
//                if (flow == null) {
//                    return null;
//                }
//                Flow result = tryForwardFlowTask(flow, flow.findNode(nodeId), userId, data);
//                return tryFowAutomate(result, data).flowVO(Collections.emptyList());
                Node handlerNode = subFlowList.stream()
                    .filter(subFlow -> subFlow.findNode(nodeId) != null)
                    .map(e -> e.findNode(nodeId))
                    .findAny()
                    .orElse(null);

                if (handlerNode == null) {
                    return mainFlow.flowVONew(subFlowList);
                }

                subFlowList.forEach(subFlow -> {
                    Node node = subFlow.findNode(handlerNode.getName());
                    if (node.getStatus() == NodeStatusEnum.ACTIVE) {
                        tryForwardFlowTask(subFlow, node, userId, data);
                        tryFowAutomate(subFlow, data);
                    }
                });
                return mainFlow.flowVONew(subFlowList);
            }
            return mainFlow.flowVONew(subFlowList);
        } finally {
            flowRepository.refreshCache(mainFlow.getId());
        }
    }


    @Override
    public List<HistoricTaskInstance> createSubFlowTask(TaskForwardDTO dto) {
        Node node = dto.getExecuteNode();
        Flow flow = dto.getMainFlow();
        // 查询异常流程
        List<Flow> exFlowList = flowRepository.listFlowByParentId(dto.getMainFlow().getId());
        List<Flow> calibrateFlowList = new ArrayList<>(exFlowList);
        calibrateFlowList.add(dto.getMainFlow());
        // 该节点是异常节点，要对过去的节点进行修改，需要新建一个流程处理
        // 判断有没有必要创建一条异常流程
        List<Flow> subList = flowRepository.listFlowByParentId(node.getFlowId());
//        if (CollectionUtils.isNotEmpty(subList)) {
//            boolean flag = subList.stream()
//                    .allMatch(subFlow -> subFlow.checkoutFlowNodeIsHandled(node.getName()));
//            if (!flag) {
//                throw new ServiceException("有流程将经过当前节点，不可变更");
//            }
//        }
        log.info("mamba flow:{}", JSON.toJSONString(flow));
        Flow subFlow = flow.subFlow();
        log.info("mamba new subFlow:{}", JSON.toJSONString(subFlow));
        subFlow.modifyNodeStatus(node);
        Node subNode = subFlow.findNode(node.getName());
        subFlow.modifyNextNodeStatus(subNode.getId(), dto.getExecuteId(), dto.getData());
        syncFlow(calibrateFlowList, subFlow);
        if (subFlow.end()) {
            subFlow.setStatus(FlowStatusEnum.END);
        }
        flowRepository.add(subFlow);
        calibrateFlowList.stream()
                .peek(calibrateFlow -> calibrateFlow.flowSelfCheck(dto.getData()))
                .forEach(calibrateFlow -> flowRepository.update(calibrateFlow));
        List<History> forwardHistory = subFlow.forwardHistory(subNode.getId(), dto.getExecuteId());
        historyRepository.add(forwardHistory);
        // 发送消息提醒
//        List<NotifyDTO> notifyDTOList = Stream.of(flow.anomalyNotifyList(), subFlow.normalNotifyList())
//                .flatMap(Collection::stream)
//                .collect(Collectors.toList());
        callback(flow.anomalyNotifyList(dto));
        callback(subFlow.normalNotifyList());

        return forwardHistory.stream()
                .map(History::toHistoricTask)
                .collect(Collectors.toList());
    }

    public Flow tryForwardFlowTask(Flow flow, Node node, Long userId, Map<String, Object> data) {
        flow.modifyNextNodeStatus(node.getId(), userId, data);
        if (flow.end()) {
            flow.setStatus(FlowStatusEnum.END);
        }
        return flow;
    }

    @Override
    public List<HistoricTaskInstance> forwardMainFlowTask(TaskForwardDTO dto) {
        boolean end = false;
        // 查询异常流程
        List<Flow> exFlowList = flowRepository.listFlowByParentId(dto.getMainFlow().getId());
        List<Flow> calibrateFlowList = new ArrayList<>(exFlowList);
        Node node = dto.getExecuteNode();
        Flow flow = dto.getMainFlow();
        flow.modifyNextNodeStatus(node.getId(), dto.getExecuteId(), dto.getData());
        syncFlow(calibrateFlowList, flow);
        if (flow.end() && (CollectionUtils.isEmpty(exFlowList) || exFlowList.stream()
                .allMatch(Flow::end))) {
            end = true;
            flow.setStatus(FlowStatusEnum.END);
            exFlowList.forEach(exFlow -> exFlow.setStatus(FlowStatusEnum.END));
            exFlowList.forEach(exFlow -> flowRepository.update(exFlow));
            log.info("流程结束发送通知");
        }
        calibrateFlowList.stream()
                .peek(calibrateFlow -> calibrateFlow.flowSelfCheck(dto.getData()))
                .forEach(calibrateFlow -> flowRepository.update(calibrateFlow));
        flowRepository.update(flow);
        // 发送消息提醒
        if (end) {
            callback(Collections.singletonList(flow.flowEndNotify()));
        }
        List<History> forwardHistory = flow.forwardHistory(node.getId(), dto.getExecuteId());
        historyRepository.add(forwardHistory);
        callback(flow.normalNotifyList());
        callback(Collections.singletonList(node.toNotifyComplete(flow)));

        return forwardHistory.stream()
                .map(History::toHistoricTask)
                .collect(Collectors.toList());
    }

    @Override
    public List<HistoricTaskInstance> forwardSubFlowTask(TaskForwardDTO dto) {
        Node node = dto.getExecuteNode();
        Flow flow = dto.getMainFlow();
        boolean end = false;
        // 查询异常流程
        List<Flow> exFlowList = flowRepository.listFlowByParentId(dto.getMainFlow().getId());
        List<Flow> calibrateFlowList = new ArrayList<>(exFlowList);
        calibrateFlowList.add(dto.getMainFlow());
        // 如果是异常流程的节点
        Flow errorFlow = exFlowList.stream()
                .filter(exFlow -> exFlow.findNode(node.getId()) != null)
                .findAny().orElse(null);
        if (errorFlow == null) {
            return Collections.emptyList();
        }
        Node errorNode = errorFlow.findNode(node.getId());
        if (errorNode.getStatus() != NodeStatusEnum.ACTIVE) {
            throw new ServiceException("该节点未被认领");
        }
        errorFlow.modifyNextNodeStatus(node.getId(), dto.getExecuteId(), dto.getData());
        syncFlow(calibrateFlowList, errorFlow);
        calibrateFlowList.stream()
                .peek(calibrateFlow -> calibrateFlow.flowSelfCheck(dto.getData()))
                .forEach(calibrateFlow -> flowRepository.update(calibrateFlow));
        if (errorFlow.end() && exFlowList.stream().allMatch(Flow::end)) {
            flow.setStatus(FlowStatusEnum.END);
            exFlowList.forEach(exFlow -> exFlow.setStatus(FlowStatusEnum.END));
            exFlowList.forEach(exFlow -> flowRepository.update(exFlow));
            flow.setStatus(FlowStatusEnum.END);
            flowRepository.update(flow);
            end = true;
            log.info("流程结束发送通知");
        } else {
            flowRepository.update(errorFlow);
        }
        List<History> forwardHistory = errorFlow.forwardHistory(node.getId(), dto.getExecuteId());
        historyRepository.add(forwardHistory);

        if (end) {
            callback(Collections.singletonList(flow.flowEndNotify()));
        }
        // 异步发送消息提醒
        callback(errorFlow.normalNotifyList());
        callback(Collections.singletonList(errorNode.toNotifyComplete(errorFlow)));

        return forwardHistory.stream()
                .map(History::toHistoricTask)
                .collect(Collectors.toList());
    }


    public void syncFlow(List<Flow> calibrateFlowList, Flow standardFlow) {
        List<NotifyDTO> notifyDTOList = new ArrayList<>();

        for (Flow flow : calibrateFlowList) {
            List<Node> nodeList = flow.calibrateFlowV2(standardFlow);
            for (Node node : nodeList) {
                // 需要通知的节点转成待办
                NotifyDTO cc = node.toNotifyCC(flow, "已不会流经该节点，您不需要再处理该节点, 已将您的待办删除");
                notifyDTOList.add(cc);
                NotifyDTO delete = node.toNotifyDelete(flow);
                notifyDTOList.add(delete);
            }
        }
        notifyDTOList.forEach(notifyDTO -> callback(Collections.singletonList(notifyDTO)));
//        callback(notifyDTOList);
    }

    private void callback(List<NotifyDTO> list) {
        list.stream()
                .filter(e -> StringUtils.isBlank(e.getCode()))
                .findAny()
                .ifPresent(e -> {
                    throw new ServiceException("终于找到你了，模版code为空，代码有bug，请检查");
                });
        if (CollectionUtils.isNotEmpty(list)) {
            CompletableFuture.runAsync(() ->
                    msgNotifyCallbacks.forEach(callback -> {
//                        callback.notify(list.stream().filter(notify -> notify.getTodoNotify() == TodoNotifyEnum.NOTIFY)
//                                .collect(Collectors.toList()));
                        callback.message(list);
                    })
            );
        }
    }

    @Override
    public FlowVO flow(Long flowId) {
        Flow flow = flowRepository.queryById(flowId);
        if (flow == null) {
            return null;
        }
        // TODO: 2023/8/9 目前历史记录只塞了主流程
        List<History> historyList = historyRepository.queryByFlowId(flowId);

        List<FlowVO> flowList = flowRepository.listFlowByParentId(flowId)
                .stream().map(e -> e.flowVO(Collections.emptyList())).collect(Collectors.toList());

        FlowVO flowVO = flow.flowVO(flowList, historyList);
        flowVO.setHasSubFlow(CollectionUtils.isNotEmpty(flowList));
        flowVO.setSubFlowCount(flowList.size());
        return flowVO;
    }


}
