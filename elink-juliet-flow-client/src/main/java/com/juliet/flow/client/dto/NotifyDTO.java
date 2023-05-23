package com.juliet.flow.client.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * NotifyDTO
 *
 * @author Geweilang
 * @date 2023/5/23
 */
@Getter
@Setter
public class NotifyDTO {
    private Long flowId;
    // TODO: 2023/5/23 暂时未提供 
    private Long mainFlowId;
    private Long nodeId;
    private String nodeName;
    private Long userId;
}
