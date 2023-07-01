package com.juliet.flow.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * UserIdDto
 *
 * @author Geweilang
 * @date 2023/5/10
 */
@Getter
@Setter
public class TaskDTO {
    private Long flowId;
    private Long nodeId;
    @Deprecated
    private String nodeName;
    private Long userId;
    private Long localUser;
    @Deprecated
    private List<Long> postIdList;
}
