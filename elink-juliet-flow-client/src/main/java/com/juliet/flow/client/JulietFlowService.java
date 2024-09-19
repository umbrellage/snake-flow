package com.juliet.flow.client;

import com.juliet.flow.client.dto.FlowIdListDTO;
import com.juliet.flow.client.dto.HistoricTaskInstance;
import com.juliet.flow.client.dto.RollbackDTO;
import com.juliet.flow.client.vo.FlowVO;

import com.juliet.flow.client.vo.GraphVO;
import java.util.List;

/**
 * @author xujianjie
 * @date 2024-04-14
 */
public interface JulietFlowService {

    List<FlowVO> flowList(FlowIdListDTO dto);

    List<HistoricTaskInstance> rollback(RollbackDTO dto);

    GraphVO graph(Long flowId, Long userId, List<Long> postIdList);

    /**
     * 查找出我是操作人的流程，
     * @param flowCode
     * @param userId
     * @return
     */
    List<FlowVO> flowListByOperator(String flowCode, Long userId, List<Long> postIdList);


    /**
     * 直接修改某个节点为待办类型
     * @param flowId
     * @param nodeId
     */
    void directTriggerTodo(Long flowId, Long nodeId);

    /**
     * 给某个岗位的节点的分配操作人
     * @param flowId
     * @param postId
     * @param userId
     */
    void distributionNodeOperator4Post(Long flowId, Long postId, Long userId);

}
