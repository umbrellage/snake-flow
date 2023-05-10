package com.juliet.flow.client.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * BpmDto
 * 发起新流程
 * @author Geweilang
 * @date 2023/5/10
 */
@Getter
@Setter
public class BpmDto {
    /**
     * 数据 id
     */
    private Long dataId;
    /**
     * 流程模版 id
     */
    private Long templateId;
}
