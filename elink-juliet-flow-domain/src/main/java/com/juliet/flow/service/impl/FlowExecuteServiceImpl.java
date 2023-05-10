package com.juliet.flow.service.impl;

import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.domain.model.Role;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.service.FlowExecuteService;
import com.juliet.flow.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Service
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
    public void startFlow(Long templateId) {
        FlowTemplate flowTemplate = flowRepository.queryTemplateById(templateId);
        Flow flow = flowTemplate.initTouYangFlow();
        flow.validate();
        flowRepository.add(flow);
    }

    @Override
    public void forward(Long flowId) {
        Flow flow = flowRepository.queryById(flowId);


        flow.forward();
//        List<Role> todoRoles = flow.get();
//        todoService.sendTodo(todoRoles, flow.getCurrentTodo());
    }
}
