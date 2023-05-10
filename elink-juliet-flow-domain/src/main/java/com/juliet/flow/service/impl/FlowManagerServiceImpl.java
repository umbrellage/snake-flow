package com.juliet.flow.service.impl;

import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.service.FlowManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Service
public class FlowManagerServiceImpl implements FlowManagerService {

    @Autowired
    private FlowRepository flowRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(FlowTemplate flowTemplate) {

    }

    @Override
    public void update(FlowTemplate flowTemplate) {
    }

    @Override
    public void publish(Long flowId) {



    }

    @Override
    public void disable(Long flowId) {

    }
}
