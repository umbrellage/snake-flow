package com.juliet.flow.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.juliet.flow.client.common.OperateTypeEnum;
import lombok.Data;

/**
 * RollbackDTO
 *
 * @author Geweilang
 * @date 2023/8/3
 */
@Data
public class RollbackDTO implements TaskExecute {
    private final OperateTypeEnum taskType = OperateTypeEnum.ROLLBACK;
    private String flowId;
    private String nodeId;
    /**
     * 操作人
     */
    private Long userId;
    /**
     * 0 回退到发起人节点, 1 回退到上一个节点，
     */
    private Integer type;
    private String reason;
}
