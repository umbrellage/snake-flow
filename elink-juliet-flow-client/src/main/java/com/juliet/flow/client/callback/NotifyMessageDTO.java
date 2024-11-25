package com.juliet.flow.client.callback;

import com.juliet.flow.client.common.NotifyTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-07-20
 */
@Data
public class NotifyMessageDTO {

    private String templateCode;
    private Long flowId;
    private Long mainFlowId;
    private NotifyTypeEnum type;
    private Long tenantId;
    /**
     * 以下两字段特殊处理
     */
    private String msg;
    private Long userId;
    @ApiModelProperty("操作人")
    private Long executorId;
}
