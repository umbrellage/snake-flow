package com.juliet.flow.client.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class FieldDTO {

    private String id;

    /**
     * 字段标识
     */
    @NotNull
    private String code;

    /**
     * 字段名称
     */
    @NotNull
    private String name;
}
