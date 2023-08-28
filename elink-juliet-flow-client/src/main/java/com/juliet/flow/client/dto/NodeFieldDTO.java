package com.juliet.flow.client.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * NodeFieldDTO
 *
 * @author Geweilang
 * @date 2023/5/16
 */
@Getter
@Setter
public class NodeFieldDTO {

    private List<String> fieldCodeList;
    private Long nodeId;
    private Long flowId;
    private Long userId;

    private Map<String, Object> data = new HashMap<>();
}
