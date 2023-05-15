package com.juliet.flow.service.impl;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.domain.model.Role;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.service.FlowExecuteService;
import com.juliet.flow.service.TodoService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
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
    private TodoService todoService;

    @Override
    public NodeVO queryStartNodeByCode(Long tenantId, String templateCode) {
        FlowTemplate template = flowRepository.queryTemplateByCode(templateCode);
        return template.getNodes().stream()
            .filter(node -> StringUtils.isBlank(node.getPreName()))
            .findAny()
            .map(e -> e.toNodeVo(null))
            .orElseThrow(()-> new ServiceException("找不到开始节点"));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long startFlow(Long templateId) {
        FlowTemplate flowTemplate = flowRepository.queryTemplateById(templateId);
        Flow flow = flowTemplate.toFlowInstance();
        flow.validate();
        flowRepository.add(flow);
        return flow.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long forward(Long flowId, Map<String, ?> map) {
        if (flowId == null) {
            Long templateId = (Long) map.entrySet().stream()
                .filter(entry -> "templateId".equals(entry.getKey()))
                .map(Entry::getValue)
                .findAny()
                .orElseThrow(() -> new ServiceException("缺少模版id"));
            return startFlow(templateId);
        }
        // 判断哪个节点需要被执行
        List<Node> executableNode = new ArrayList<>();
        Flow flow = flowRepository.queryById(flowId);
        List<Flow> subFlowList = flowRepository.listFlowByParentId(flowId);
        Node mainNode = flow.findNode(map);
        if (CollectionUtils.isEmpty(subFlowList)) {
            if (mainNode.isExecutable()) {
                executableNode.add(mainNode);
            } else {
                log.info("[node code, is not executable] {}", mainNode.getName());
                throw new ServiceException("当前节点不可被执行");
            }
        }else {
            subFlowList.stream()
                .filter(subFlow -> {
                    Node node = subFlow.findNode(map);
                    return node.isExecutable() && subFlow.ifPreNodeIsHandle(node.getName());
                })
                .forEach(subFlow -> executableNode.add(subFlow.findNode(map)));
        }
        executableNode.forEach(node -> task(flowId, mainNode.getId(), mainNode.getName(), mainNode.getProcessedBy()));

        return null;
    }

    @Override
    public List<NodeVO> currentNodeList(Long flowId) {
        Flow flow = flowRepository.queryById(flowId);
        if (flow == null) {
            return Collections.emptyList();
        }
        List<NodeStatusEnum> nodeStatusEnumList = Stream.of(NodeStatusEnum.TO_BE_CLAIMED, NodeStatusEnum.ACTIVE)
            .collect(Collectors.toList());
        return flow.getNodeByNodeStatus(nodeStatusEnumList)
            .stream()
            .map(node -> node.toNodeVo(flowId))
            .collect(Collectors.toList());
    }

    @Override
    public void claimTask(Long flowId, Long nodeId, Long userId) {
        Flow flow = flowRepository.queryById(flowId);
        BusinessAssert.assertNotNull(flow, StatusCode.SERVICE_ERROR, "can not find flow, flowId:" + flowId);
        flow.claimNode(nodeId, userId);
        flowRepository.update(flow);
    }

    @Override
    public List<NodeVO> todoNodeList(Long userId) {
        // TODO: 2023/5/11  
        return null;
    }

    /**
     *
     * 该方法主要分4种情况
     * 待处理的节点主要分为两种节点:
     * 一. 主流程中的节点
     * 1. 当前要处理的节点为异常节点，且存在异常流程并未结 ------>抛出异常 TODO: 2023/5/11 后面如果支持多条异常流程则把 1的处理删除
     * 2. 当前要处理的节点为异常节点，（存在异常流程且异常流程都已结束）或者 （不存在异常流程） -------> 创建一条异常流程
     * 3. 当前要处理的节点为非异常节点 ---------> 正常处理节点的逻辑走
     * 二. 异常流程中的节点
     * 4. 当前节点为异常流程中的节点，且该异常流程并未结束 --------> 按照处理异常流程和正常流程的方式处理, 是否需要合并
     * TODO: 2023/5/11  当前认为只有一条异常流程存在，如果后面要做多条再修改
     * @param flowId
     * @param nodeId
     * @param nodeName
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void task(Long flowId, Long nodeId, String nodeName, Long userId) {
        Flow flow = flowRepository.queryById(flowId);
        if (flow == null) {
            return;
        }
        // 查询异常流程
        List<Flow> exFlowList = flowRepository.listFlowByParentId(flowId);
        // 获取要处理的节点信息，该节点可能有两种情况 1. 他是主流程的节点，2. 他是异常子流程的节点
        Node node = flow.findNode(nodeId);
        // 如果是主流程的节点
        if (node != null) {
            if (!node.isExecutable()) {
                throw new ServiceException("该节点未被认领");
            }
            // 判断 存在异常流程，且异常流程已全部结束
            boolean existsAnomalyFlowsAndFlowsEnd = CollectionUtils.isNotEmpty(exFlowList) && exFlowList.stream().allMatch(Flow::isFlowEnd);
            // 判断 存在异常流程，且异常流程没有结束
            boolean existsAnomalyFlowsAndFlowsNotEnd = CollectionUtils.isNotEmpty(exFlowList) && !exFlowList.stream().allMatch(Flow::isFlowEnd);
            // 当节点是异常节点时
            if (node.isProcessed()) {
                if (existsAnomalyFlowsAndFlowsNotEnd) {
                    throw new ServiceException("已经存在异常流程正在流转中，请等待异常流程流转完成后再进行修改", StatusCode.SERVICE_ERROR.getStatus());
                }
                if (existsAnomalyFlowsAndFlowsEnd || CollectionUtils.isEmpty(exFlowList)) {
                    // 该节点是异常节点，要对过去的节点进行修改，需要新建一个流程处理
                    Flow subFlow = flow.subFlow();
                    subFlow.modifyNodeStatus(node);
                    flowRepository.update(subFlow);
                    return;
                }
            }
            // 当节点是非异常节点时, 因为是主流程的节点，主流程不关心是否需要合并异常流程，这个操作让异常流程去做，因为异常流程在创建是肯定比主流程慢
            // 主流程只需要判断下是否存在异常流程为结束，如果存在，主流程在完成整个流程前等待异常流程合并至主流程
            if (!node.isProcessed()) {
                flow.finishNode(nodeId);
                if (flow.isEnd() && (CollectionUtils.isEmpty(exFlowList) || exFlowList.stream().allMatch(Flow::isEnd))) {
                    flow.setStatus(FlowStatusEnum.END);
                }
                flowRepository.update(flow);
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
            errorFlow.finishNode(nodeId);
            if (errorFlow.isEnd()) {
                errorFlow.setStatus(FlowStatusEnum.END);
                // 如果子流程都结束了，那么主流程也肯定结束了
                flow.setStatus(FlowStatusEnum.END);
                flowRepository.update(flow);
            }
            flowRepository.update(errorFlow);
        }

    }

    @Override
    public FlowVO flow(Long flowId) {
        Flow flow = flowRepository.queryById(flowId);
        if (flow == null) {
            return null;
        }
        return flow.flowVO();
    }
}
