package com.juliet.flow.client;

import com.juliet.flow.client.dto.FlowIdListDTO;
import com.juliet.flow.client.dto.HistoricTaskInstance;
import com.juliet.flow.client.dto.RollbackDTO;
import com.juliet.flow.client.dto.TaskExecute;
import com.juliet.flow.client.vo.FlowVO;

import java.util.List;

/**
 * @author xujianjie
 * @date 2024-04-14
 */
public interface JulietFlowService {

    List<FlowVO> flowList(FlowIdListDTO dto);

    List<HistoricTaskInstance> rollback(RollbackDTO dto);
}
