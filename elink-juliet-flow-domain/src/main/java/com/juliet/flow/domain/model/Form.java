package com.juliet.flow.domain.model;

import lombok.Data;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Data
public class Form {

    private Long id;

    private String name;

    private String code;

    private String path;

    private List<Field> fields;
}
