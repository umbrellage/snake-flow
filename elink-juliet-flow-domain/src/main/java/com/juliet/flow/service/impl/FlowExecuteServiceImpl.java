package com.juliet.flow.service.impl;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.common.StatusCode;
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
    public Node queryStartNodeByCode(Long tenantId, String templateCode) {
        return null;
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
    public void forward(Long flowId) {
        Flow flow = flowRepository.queryById(flowId);

        flow.forward();
//        List<Role> todoRoles = flow.get();
//        todoService.sendTodo(todoRoles, flow.getCurrentTodo());
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
     * 1. 当前要处理的节点为异常节点，且存在异常流程并未结 ------>抛出异常
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
                if (existsAnomalyFlowsAndFlowsNotEnd) {
                    // TODO: 2023/5/11 当前认为只有一条异常流程存在，如果后面要做多条只需要在这个方法里修改就好
                    Flow errorFlow = exFlowList.stream().filter(exFlow -> !exFlow.isEnd())
                        .findAny()
                        .orElse(null);





                }
                if (existsAnomalyFlowsAndFlowsEnd && CollectionUtils.isEmpty(exFlowList)) {
                    flow.finishNode(nodeId);
                    flow.isEnd();
                    flowRepository.update(flow);
                }
            }
        } else {
            // 如果是异常流程的节点

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
