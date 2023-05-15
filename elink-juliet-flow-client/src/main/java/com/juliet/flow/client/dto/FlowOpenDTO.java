package com.juliet.flow.client.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class FlowOpenDTO {

    private String code;

    /**
     * 可以指定某一个发布的模板，不指定时默认取最新版
     */
    private Long templateId;
}
