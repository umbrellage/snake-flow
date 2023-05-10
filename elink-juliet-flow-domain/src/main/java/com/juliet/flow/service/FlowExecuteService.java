package com.juliet.flow.service;

import com.juliet.flow.domain.model.Node;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
public interface FlowExecuteService {

    Node queryStartNodeByCode(Long tenantId, String templateCode);

    void startFlow(Long templateId);

    void forward(Long flowId);
}
