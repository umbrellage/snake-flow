package com.juliet.flow.client.dto;

import com.juliet.flow.client.common.OperateTypeEnum;
import lombok.Data;

/**
 * RejectDTO
 *
 * @author Geweilang
 * @date 2023/8/9
 */
@Data
public class RejectDTO implements TaskExecute{

    private String flowId;
    private String nodeId;
    private Long userId;
    private String reason;

    @Override
    public OperateTypeEnum getTaskType() {
        return OperateTypeEnum.REJECT;
    }
}
