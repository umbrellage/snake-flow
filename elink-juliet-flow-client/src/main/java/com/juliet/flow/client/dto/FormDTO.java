package com.juliet.flow.client.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class FormDTO {

    private String id;

    /**
     * 表单中文名称
     */
    @NotNull
    private String name;

    /**
     * 表单的标识，英文名
     */
    @NotNull
    private String code;

    /**
     * 跳转路径
     */
    @NotNull
    private String path;

    @NotNull
    private List<FieldDTO> fields;
}
