package com.juliet.flow.dubbo;

import com.juliet.flow.client.JulietFlowService;
import com.juliet.flow.client.dto.FlowIdListDTO;
import com.juliet.flow.client.dto.HistoricTaskInstance;
import com.juliet.flow.client.dto.RollbackDTO;
import com.juliet.flow.client.dto.TaskExecute;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.GraphVO;
import com.juliet.flow.service.FlowExecuteService;
import com.juliet.flow.service.FlowManagerService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xujianjie
 * @date 2024-04-14
 */
@Service
@DubboService(timeout = 10000, mock = "force:return+null")
public class JulietFlowServiceImpl implements JulietFlowService {

    @Autowired
    private FlowExecuteService flowExecuteService;
    @Autowired
    private FlowManagerService flowManagerService;

    @Override
    public List<FlowVO> flowList(FlowIdListDTO dto) {
        return flowExecuteService.flowList(dto);
    }

    @Override
    public List<HistoricTaskInstance> rollback(RollbackDTO dto) {
        return flowExecuteService.execute(dto);
    }

    @Override
    public GraphVO graph(Long flowId, Long userId, List<Long> postIdList) {
        return flowManagerService.getGraph(flowId, userId, postIdList);
    }
}
