package com.juliet.flow.service.impl;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import com.alibaba.fastjson2.JSON;
import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.callback.MsgNotifyCallback;
import com.juliet.flow.client.dto.BpmDTO;
import com.juliet.flow.client.dto.FlowIdListDTO;
import com.juliet.flow.client.dto.FlowOpenDTO;
import com.juliet.flow.client.dto.InvalidDTO;
import com.juliet.flow.client.dto.NodeFieldDTO;
import com.juliet.flow.client.dto.NotifyDTO;
import com.juliet.flow.client.dto.RollbackDTO;
import com.juliet.flow.client.dto.TaskDTO;
import com.juliet.flow.client.dto.TaskExecute;
import com.juliet.flow.client.dto.UserDTO;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;
import com.juliet.flow.domain.model.History;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.domain.model.NodeQuery;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.repository.HistoryRepository;
import com.juliet.flow.service.FlowExecuteService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Service
@Slf4j
public class FlowExecuteServiceImpl implements FlowExecuteService {

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private List<MsgNotifyCallback> msgNotifyCallbacks;
    @Autowired
    private HistoryRepository historyRepository;

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
        if (node.postAuthority(dto.getPostIdList())) {
            return node.toNodeVo(null);
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
        Node node = flow.startNode();
        flow.modifyNextNodeStatus(node.getId(), dto.getData());
        flow.validate();
        flowRepository.add(flow);
        Flow dbFlow = flowRepository.queryById(flow.getId());
        callback(dbFlow.normalNotifyList());
        return flow.getId();
    }

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

    @Override
    public NodeVO node(TaskDTO dto) {
        Flow flow = flowRepository.queryById(dto.getFlowId());
        if (flow == null) {
            return null;
        }
        Optional.ofNullable(dto.getUserId()).orElseThrow(() -> new ServiceException("用户id 不可以为空"));
        Node node;
        if (dto.getNodeId() != null) {
            node = flow.findNode(dto.getNodeId());
        } else {
            node = flow.findTodoNode(dto.getUserId());
        }

        if (node == null) {
            node = findSubFlowList(flow.getId()).stream()
                .map(subFlow -> subFlow.findTodoNode(dto.getUserId()))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
        }
        if (node == null) {
            return null;
        }

        if (CollectionUtils.isEmpty(dto.getPostIdList())) {
            return node.toNodeVo(null);
        }
        return node.postAuthority(dto.getPostIdList()) ? node.toNodeVo(null) : null;
    }

    @Override
    public List<FlowVO> flowList(FlowIdListDTO dto) {
        if (CollectionUtils.isEmpty(dto.getFlowIdList())) {
            log.error("流程ID列表为空");
            return Collections.emptyList();
        }
        List<Flow> mainFlowList = flowRepository.queryByIdList(dto.getFlowIdList());
        List<Long> flowIdList = mainFlowList.stream().map(Flow::getId).collect(Collectors.toList());
        Map<Long, List<FlowVO>> subFlowMap = flowRepository.listFlowByParentId(flowIdList)
            .stream().map(flow -> flow.flowVO(Collections.emptyList())).collect(Collectors.groupingBy(FlowVO::getParentId));
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
        return flowTemplate.getNodes().stream()
            .map(Node::getCustomStatus)
            .filter(StringUtils::isNotBlank).distinct()
            .collect(Collectors.toList());
    }

    @Override
    public void execute(TaskExecute dto) {
        switch (dto.getTaskType()) {
            case ROLLBACK:
                rollback(dto);
                break;
            default:
                break;
        }
    }

    @Override
    public void invalid(InvalidDTO dto) {
        // TODO: 2023/8/4
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
        History history = History.of(rollback, node.getId(), node.getTenantId());
        historyRepository.add(history);
    }


    private List<Flow> findSubFlowList(Long id) {
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
        BusinessAssert.assertTrue(node.ifLeaderAdjust(dto.getLocalUser()), StatusCode.SERVICE_ERROR, "当前操作人没有权限调整");
        node.setProcessedBy(dto.getUserId());
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
        List<Node> userIdNodeList = flowRepository.listNode(NodeQuery.findByUserId(dto.getUserId()));
        List<Node> supervisorIdNodeList = flowRepository.listNode(NodeQuery.findBySupervisorId(dto.getUserId()));
        List<Node> postIdNodeList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dto.getPostId())) {
            postIdNodeList = flowRepository.listNode(NodeQuery.findByPostId(dto.getPostId())).stream()
                .filter(node -> node.getProcessedBy() == null || node.getProcessedBy().longValue() == 0L)
                .collect(Collectors.toList());
        }
        List<Node> supplierNodeList = new ArrayList<>();
        if (dto.getSupplier() != null) {
            supplierNodeList = flowRepository.listNode(dto.supplierId(), dto.supplierType()).stream()
                .filter(node -> node.getProcessedBy() == null || node.getProcessedBy() == 0L)
                .collect(Collectors.toList());
        }
        List<Long> flowIdList = Stream.of(userIdNodeList, postIdNodeList, supervisorIdNodeList, supplierNodeList)
            .flatMap(Collection::stream)
            .map(Node::getFlowId)
            .distinct()
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(flowIdList)) {
            return Collections.emptyList();
        }

        Map<Long, Flow> flowMap = flowRepository.queryByIdList(flowIdList).stream()
            .collect(Collectors.toMap(Flow::getId, Function.identity()));

        List<NodeVO> nodeVOList = Stream.of(userIdNodeList, postIdNodeList, supervisorIdNodeList)
            .flatMap(Collection::stream)
            .map(node -> node.toNodeVo(flowMap.get(node.getFlowId())))
            .collect(Collectors.toList());

        return nodeVOList.stream().collect(collectingAndThen(toCollection(() ->
            new TreeSet<>(Comparator.comparing(NodeVO::distinct))), ArrayList::new));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forward(NodeFieldDTO dto) {
        // 需要被执行的节点列表
        List<Node> executableNode = new ArrayList<>();
        Flow flow = flowRepository.queryById(dto.getFlowId());
        if (flow == null) {
            return;
        }
        Node currentFlowNode = flowRepository.queryNodeById(dto.getNodeId());
//        callback(Collections.singletonList(currentFlowNode.toNotifyComplete(flow)));
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
        if (mainFlow.isFlowEnd()) {
            throw new ServiceException("流程已结束");
        }
        subFlowList.add(mainFlow);
        if (CollectionUtils.isNotEmpty(subFlowList)) {
            subFlowList.stream()
                .filter(subFlow -> {
                    Node node = subFlow.findNode(currentFlowNode.getName());
                    return node.isNormalExecutable() && subFlow.ifPreNodeIsHandle(node.getName())
                        && node.getStatus() != NodeStatusEnum.IGNORE;
                })
                .forEach(subFlow -> executableNode.add(subFlow.findNode(currentFlowNode.getName())));
        }

        executableNode.add(currentFlowNode);

        List<Node> nodeList = new ArrayList<>(
            executableNode.stream().collect(Collectors.toMap(Node::getId, Function.identity(), (v1, v2) -> v1))
                .values());

        for (Node node : nodeList) {
            task(mainFlow.getId(), node.getId(), node.getProcessedBy(), dto.getData());
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
     * @param flowId 主流程节点
     * @param nodeId
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void task(Long flowId, Long nodeId, Long userId, Map<String, Object> data) {
        Flow flow = flowRepository.queryById(flowId);
        if (flow == null || flow.hasParentFlow()) {
            log.error("流程存在异常{}", JSON.toJSONString(flow));
            throw new ServiceException("流程存在异常");
        }
        // 查询异常流程
        List<Flow> exFlowList = flowRepository.listFlowByParentId(flowId);
        List<Flow> calibrateFlowList = new ArrayList<>(exFlowList);
        calibrateFlowList.add(flow);
        // 获取要处理的节点信息，该节点可能有两种情况 1. 他是主流程的节点，2. 他是异常子流程的节点
        Node node = flow.findNode(nodeId);
        boolean end = false;
        // 如果是主流程的节点
        if (node != null) {
            if (!node.isExecutable()) {
                return;
            }
            // 当节点是异常节点时
            if (node.isProcessed()) {
                // 该节点是异常节点，要对过去的节点进行修改，需要新建一个流程处理
                // 判断有没有必要创建一条异常流程
                List<Flow> subList = flowRepository.listFlowByParentId(node.getFlowId());
                if (CollectionUtils.isNotEmpty(subList)) {
                    boolean flag = subList.stream().allMatch(subFlow -> subFlow.checkoutFlowNodeIsHandled(node.getName()));
                    if (!flag) {
                        throw new ServiceException("有流程将经过当前节点，不可变更");
                    }
                }
                Flow subFlow = flow.subFlow();
                subFlow.modifyNodeStatus(node);
                Node subNode = subFlow.findNode(node.getName());
                subFlow.modifyNextNodeStatus(subNode.getId(), data);
                syncFlow(calibrateFlowList, subFlow);
                flowRepository.add(subFlow);
                // 发送消息提醒
                List<NotifyDTO> notifyDTOList = Stream.of(flow.anomalyNotifyList(), subFlow.normalNotifyList())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
                callback(notifyDTOList);
                return;
            }
            // 当节点是非异常节点时, 因为是主流程的节点，主流程不关心是否需要合并异常流程，这个操作让异常流程去做，因为异常流程在创建是肯定比主流程慢
            // 主流程只需要判断下是否存在异常流程为结束，如果存在，主流程在完成整个流程前等待异常流程合并至主流程
            if (!node.isProcessed()) {
                flow.modifyNextNodeStatus(nodeId, data);
                syncFlow(calibrateFlowList, flow);
                if (flow.isEnd() && (CollectionUtils.isEmpty(exFlowList) || exFlowList.stream()
                    .allMatch(Flow::isEnd))) {
                    end = true;
                    flow.setStatus(FlowStatusEnum.END);
                    exFlowList.forEach(exFlow -> exFlow.setStatus(FlowStatusEnum.END));
                    exFlowList.forEach(exFlow -> flowRepository.update(exFlow));
                    log.info("流程结束发送通知");
                }
                flowRepository.update(flow);
                // 发送消息提醒
                if (end) {
                    callback(Collections.singletonList(flow.flowEndNotify()));
                }
                callback(flow.normalNotifyList());
                callback(Collections.singletonList(node.toNotifyComplete(flow)));
            }
        }
        if (node == null) {
            // 如果是异常流程的节点
            Flow errorFlow = exFlowList.stream()
                .filter(exFlow -> exFlow.findNode(nodeId) != null)
                .findAny().orElse(null);
            if (errorFlow == null) {
                return;
            }
            Node errorNode = errorFlow.findNode(nodeId);
            if (errorNode.getStatus() != NodeStatusEnum.ACTIVE) {
                throw new ServiceException("该节点未被认领");
            }
            errorFlow.modifyNextNodeStatus(nodeId, data);
            syncFlow(calibrateFlowList, errorFlow);
            if (errorFlow.isEnd() && exFlowList.stream().allMatch(Flow::isEnd)) {
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

            if (end) {
                callback(Collections.singletonList(flow.flowEndNotify()));
            }
            // 异步发送消息提醒
            callback(errorFlow.normalNotifyList());
            callback(Collections.singletonList(errorNode.toNotifyComplete(errorFlow)));
        }
    }

    public void syncFlow(List<Flow> calibrateFlowList, Flow standardFlow) {
        List<NotifyDTO> notifyDTOList = calibrateFlowList.stream()
            .map(flow -> flow.calibrateFlow(standardFlow))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        callback(notifyDTOList);
    }

    private void callback(List<NotifyDTO> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            CompletableFuture.runAsync(() -> msgNotifyCallbacks.forEach(callback -> callback.notify(list)));
        }
    }

    @Override
    public FlowVO flow(Long flowId) {
        Flow flow = flowRepository.queryById(flowId);
        if (flow == null) {
            return null;
        }
        List<FlowVO> flowList = flowRepository.listFlowByParentId(flowId)
            .stream().map(e -> e.flowVO(Collections.emptyList())).collect(Collectors.toList());
        FlowVO flowVO = flow.flowVO(flowList);
        flowVO.setHasSubFlow(CollectionUtils.isNotEmpty(flowList));
        flowVO.setSubFlowCount(flowList.size());
        return flowVO;
    }


}
