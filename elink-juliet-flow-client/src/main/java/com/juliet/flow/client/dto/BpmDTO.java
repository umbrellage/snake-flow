package com.juliet.flow.client.dto;

import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * BpmDto
 * 发起新流程
 * @author Geweilang
 * @date 2023/5/10
 */
@Getter
@Setter
public class BpmDTO {
    /**
     * 流程模版 code
     */
    private String templateCode;
    private Long userId;
    private Long tenantId;

    private Map<String, Object> data  = new HashMap<>();

    public Map<String, Object> getData() {
        if (data == null) {
            return new HashMap<>();
        }
        return data;
    }
}
