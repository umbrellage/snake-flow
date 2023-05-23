package com.juliet.flow.client.dto;

import com.juliet.flow.client.common.NotifyTypeEnum;
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
    private Long mainFlowId;
    private Long nodeId;
    private String nodeName;
    private Long userId;
    private List<String> postIdList;
    private NotifyTypeEnum type;
}
