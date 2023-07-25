package com.juliet.flow.client.callback;

import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-07-20
 */
@Data
public class NotifyMessageDTO {

    private String templateCode;
    private Long flowId;
}
