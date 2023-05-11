package com.juliet.flow.service.impl;

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
    public List<NodeVO> todoNodeList(Long tenantId, Long userId) {
        // TODO: 2023/5/11  
        return null;
    }

    @Override
    public void task(Long flowId, Long nodeId, Long userId) {

    }
}
