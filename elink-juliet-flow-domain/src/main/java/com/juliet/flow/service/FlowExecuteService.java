package com.juliet.flow.service;

import com.juliet.common.core.web.domain.AjaxResult;
import com.juliet.flow.client.dto.BpmDTO;
import com.juliet.flow.client.dto.FlowIdListDTO;
import com.juliet.flow.client.dto.FlowOpenDTO;
import com.juliet.flow.client.dto.NodeFieldDTO;
import com.juliet.flow.client.dto.TaskDTO;
import com.juliet.flow.client.dto.TaskExecute;
import com.juliet.flow.client.dto.UserDTO;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.NodeVO;
import java.util.List;
import java.util.Map;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
public interface FlowExecuteService {

    NodeVO queryStartNodeById(FlowOpenDTO dto);

    Long startFlow(BpmDTO dto);

    Long startOnlyFlow(BpmDTO dto);

    boolean flowEnd(Long flowId);

    void forward(NodeFieldDTO dto);

    List<NodeVO> currentNodeList(Long flowId);

    void claimTask(TaskDTO dto);

    List<NodeVO> todoNodeList(UserDTO dto);

    void task(Long flowId, Long nodeId, Long userId, Map<String, Object> data);

    FlowVO flow(Long flowId);

    NodeVO fieldNode(NodeFieldDTO dto);

    NodeVO node(TaskDTO dto);

    List<FlowVO> flowList(FlowIdListDTO dto);

    NodeVO findNodeByFlowIdAndNodeId(TaskDTO dto);

    List<String> customerStatus(String code, Long tenantId);

    void execute(TaskExecute dto);
}
