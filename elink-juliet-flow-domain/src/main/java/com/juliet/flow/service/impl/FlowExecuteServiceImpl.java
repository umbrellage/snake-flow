package com.juliet.flow.service.impl;

import static java.util.stream.Collectors.toCollection;

import com.alibaba.fastjson2.JSON;
import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.callback.MsgNotifyCallback;
import com.juliet.flow.client.dto.BpmDTO;
import com.juliet.flow.client.dto.FlowIdListDTO;
import com.juliet.flow.client.dto.FlowOpenDTO;
import com.juliet.flow.client.dto.NodeFieldDTO;
import com.juliet.flow.client.dto.NotifyDTO;
import com.juliet.flow.client.dto.TaskDTO;
import com.juliet.flow.client.dto.UserDTO;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.domain.model.NodeQuery;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.service.FlowExecuteService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
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

    @Override
    public NodeVO queryStartNodeById(FlowOpenDTO dto) {
        FlowTemplate template = null;
        if (dto.getTemplateId() != null) {
            template = flowRepository.queryTemplateById(dto.getTemplateId());
        }
        if (StringUtils.isNotBlank(dto.getCode())) {
            template = flowRepository.queryTemplateByCode(dto.getCode());
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
        FlowTemplate flowTemplate = flowRepository.queryTemplateByCode(dto.getTemplateCode());
        if (flowTemplate == null) {
            throw new ServiceException("流程模版不存在");
        }
        Flow flow = flowTemplate.toFlowInstance(dto.getUserId());
        flow.validate();
        flowRepository.add(flow);
        CompletableFuture.runAsync(
            () -> msgNotifyCallbacks.forEach(callback -> callback.notify(flow.anomalyNotifyList())));
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

        if (node != null && node.postAuthority(dto.getPostIdList())) {
            return node.toNodeVo(null);
        }
        return null;
    }

    @Override
    public List<FlowVO> flowList(FlowIdListDTO dto) {
        if (CollectionUtils.isEmpty(dto.getFlowIdList())) {
            log.error("流程ID列表为空");
            return Collections.emptyList();
        }
        return flowRepository.queryByIdList(dto.getFlowIdList()).stream()
            .map(Flow::flowVO)
            .collect(Collectors.toList());
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
            .collect(Collectors.collectingAndThen(toCollection(() ->
                new TreeSet<>(Comparator.comparing(NodeVO::getName))), ArrayList::new));
    }

    /**
     * @param flowId 可能是主流程id，也可能是异常流程id，如果是异常流程id，找到主流程然后认领主流程下所有异常流程的节点
     * @param nodeId
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimTask(Long flowId, Long nodeId, Long userId) {
        Flow flow = flowRepository.queryById(flowId);
        if (flow == null) {
            throw new ServiceException("流程不存在");
        }
        Node node = flow.findNodeThrow(nodeId);
        if (flow.hasParentFlow()) {
            flow = flowRepository.queryById(flow.getParentId());
        }
        flow.claimNode(node.getName(), userId);
        List<Flow> subFlowList = flowRepository.listFlowByParentId(flowId);
        subFlowList.forEach(subFlow -> {
            subFlow.claimNode(node.getName(), userId);
            flowRepository.update(subFlow);
        });
        flowRepository.update(flow);
    }

    @Override
    public List<NodeVO> todoNodeList(UserDTO dto) {
        List<Node> userIdNodeList = flowRepository.listNode(NodeQuery.findByUserId(dto.getUserId()));
        List<Node> postIdNodeList = flowRepository.listNode(NodeQuery.findByPostId(dto.getPostId())).stream()
            .filter(node -> node.getProcessedBy() == null || node.getProcessedBy().longValue() == 0L)
            .collect(Collectors.toList());
        List<Long> flowIdList = Stream.of(userIdNodeList, postIdNodeList)
            .flatMap(Collection::stream)
            .map(Node::getFlowId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Flow> flowMap = flowRepository.queryByIdList(flowIdList).stream()
            .collect(Collectors.toMap(Flow::getId, Function.identity()));

        return Stream.of(userIdNodeList, postIdNodeList)
            .flatMap(Collection::stream)
            .map(node -> node.toNodeVo(flowMap.get(node.getFlowId())))
            .collect(Collectors.toList());
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
                    return node.isNormalExecutable() && subFlow.ifPreNodeIsHandle(node.getName());
                })
                .forEach(subFlow -> executableNode.add(subFlow.findNode(currentFlowNode.getName())));
        }

        executableNode.add(currentFlowNode);

        List<Node> nodeList = new ArrayList<>(
            executableNode.stream().collect(Collectors.toMap(Node::getId, Function.identity(), (v1, v2) -> v1))
                .values());

        for (Node node : nodeList) {
            task(mainFlow.getId(), node.getId(), node.getName(), node.getProcessedBy());
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
     * @param flowId   主流程节点
     * @param nodeId
     * @param nodeName
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void task(Long flowId, Long nodeId, String nodeName, Long userId) {
        Flow flow = flowRepository.queryById(flowId);
        if (flow == null || flow.hasParentFlow()) {
            log.error("流程存在异常{}", JSON.toJSONString(flow));
            throw new ServiceException("流程存在异常");
        }
        // 查询异常流程
        List<Flow> exFlowList = flowRepository.listFlowByParentId(flowId);
        // 获取要处理的节点信息，该节点可能有两种情况 1. 他是主流程的节点，2. 他是异常子流程的节点
        Node node = flow.findNode(nodeId);
        // 如果是主流程的节点
        if (node != null) {
            if (!node.isExecutable()) {
                return;
            }
            // 判断 存在异常流程，且异常流程大于10条
            boolean existsAnomalyFlowsAndFlowsNotEnd =
                CollectionUtils.isNotEmpty(exFlowList) && exFlowList.size() > 10;
            // 当节点是异常节点时
            if (node.isProcessed()) {
                if (existsAnomalyFlowsAndFlowsNotEnd) {
                    log.error("已经存在10条异常流程");
                    throw new ServiceException("已经存在异常流程正在流转中，请等待异常流程流转完成后再进行修改", StatusCode.SERVICE_ERROR.getStatus());
                }
                // 该节点是异常节点，要对过去的节点进行修改，需要新建一个流程处理
                Flow subFlow = flow.subFlow();
                subFlow.modifyNodeStatus(node);
                Node subNode = subFlow.findNode(node.getName());
                subFlow.modifyNextNodeStatus(subNode.getId());
                flowRepository.add(subFlow);
                // TODO: 2023/5/23
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
                flow.modifyNextNodeStatus(nodeId);
                if (flow.isEnd() && (CollectionUtils.isEmpty(exFlowList) || exFlowList.stream()
                    .allMatch(Flow::isEnd))) {
                    flow.setStatus(FlowStatusEnum.END);
                    exFlowList.forEach(exFlow -> exFlow.setStatus(FlowStatusEnum.END));
                    exFlowList.forEach(exFlow -> flowRepository.update(exFlow));
                }
                flowRepository.update(flow);
                // 发送消息提醒
                callback(flow.normalNotifyList());
            }
        } else {
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
            errorFlow.modifyNextNodeStatus(nodeId);
            if (errorFlow.isEnd()) {
                errorFlow.setStatus(FlowStatusEnum.END);
                // 如果子流程都结束了，那么主流程也肯定结束了
                flow.setStatus(FlowStatusEnum.END);
                flowRepository.update(flow);
            }
            flowRepository.update(errorFlow);

            // 异步发送消息提醒
            callback(errorFlow.anomalyNotifyList());
        }
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
        FlowVO flowVO = flow.flowVO();
        List<Flow> flowList = flowRepository.listFlowByParentId(flowId);
        if (CollectionUtils.isNotEmpty(flowList)) {
            flowVO.setHasSubFlow(true);
        } else {
            flowVO.setHasSubFlow(false);
        }
        return flowVO;
    }


}
