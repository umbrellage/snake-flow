package com.juliet.flow.service;

import com.juliet.flow.client.dto.FlowOpenDTO;
import com.juliet.flow.client.dto.NodeFieldDTO;
import com.juliet.flow.client.dto.UserDTO;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
public interface FlowExecuteService {

    NodeVO queryStartNodeById(FlowOpenDTO dto);

    Long startFlow(String templateCode);

    boolean flowEnd(Long flowId);

    void forward(NodeFieldDTO dto);

    List<NodeVO> currentNodeList(Long flowId);

    void claimTask(Long flowId, Long nodeId, Long userId);

    List<NodeVO> todoNodeList(UserDTO dto);

    void task(Long flowId, Long nodeId, String nodeName, Long userId);

    FlowVO flow(Long flowId);

    NodeVO fieldNode(NodeFieldDTO dto);
}
