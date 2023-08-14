package com.juliet.flow.client.dto;

import com.juliet.flow.client.common.OperateTypeEnum;
import lombok.Data;

/**
 * ForwardDTO
 *
 * @author Geweilang
 * @date 2023/8/14
 */
@Data
public class ForwardDTO implements TaskExecute{

    private Long flowId;
    private Long nodeId;
    /**
     * 操作人
     */
    private Long userId;

    @Override
    public OperateTypeEnum getTaskType() {
        return OperateTypeEnum.FORWARD;
    }


    @Deprecated
    public static ForwardDTO of(Long flowId, Long nodeId, Long userId) {
        ForwardDTO data = new ForwardDTO();
        data.setFlowId(flowId);
        data.setNodeId(nodeId);
        data.setUserId(userId);
        return data;
    }
}
