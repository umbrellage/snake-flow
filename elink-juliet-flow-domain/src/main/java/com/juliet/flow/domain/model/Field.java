package com.juliet.flow.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Field extends BaseModel {

    private Long id;

    private String code;

    private String name;
}
