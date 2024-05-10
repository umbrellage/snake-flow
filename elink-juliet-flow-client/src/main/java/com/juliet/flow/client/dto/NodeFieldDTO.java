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
    // 设置true时不要创建异常流程，如果是主流程需要仍然往后走
    private Boolean skipCreateSubFlow;

    private Map<String, Object> data = new HashMap<>();

    public Map<String, Object> getData() {
        if (data == null) {
            return new HashMap<>();
        }
        return data;
    }
}
