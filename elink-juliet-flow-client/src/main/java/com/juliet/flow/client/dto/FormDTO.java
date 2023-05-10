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
    private String name;

    /**
     * 表单的标识，英文名
     */
    private String code;

    /**
     * 跳转路径
     */
    private String path;

    private List<FieldDTO> fields;
}
