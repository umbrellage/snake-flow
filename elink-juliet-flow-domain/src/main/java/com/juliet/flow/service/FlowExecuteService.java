package com.juliet.flow.service;

import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.domain.model.Node;
import java.util.List;
import java.util.Map;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
public interface FlowExecuteService {

    NodeVO queryStartNodeByCode(Long tenantId, String templateCode);

    Long startFlow(Long templateId);

    Long forward(Long flowId, Map<String, ?> map);

    List<NodeVO> currentNodeList(Long flowId);

    void claimTask(Long flowId, Long nodeId, Long userId);

    List<NodeVO> todoNodeList(Long userId);

    void task(Long flowId, Long nodeId, String nodeName, Long userId);

    FlowVO flow(Long flowId);
}
