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
    private String nodeName;
    private Long userId;
    private List<Long> postIdList;
}
