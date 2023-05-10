package com.juliet.flow.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class FormDTO {

    private String name;

    private String formId;

    private List<FieldDTO> fields;
}
