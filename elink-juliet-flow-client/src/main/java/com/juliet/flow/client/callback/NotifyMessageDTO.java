package com.juliet.flow.client.callback;

import com.juliet.flow.client.common.NotifyTypeEnum;
import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-07-20
 */
@Data
public class NotifyMessageDTO {

    private String templateCode;
    private Long flowId;
    private NotifyTypeEnum type;
    private Long tenantId;
}
