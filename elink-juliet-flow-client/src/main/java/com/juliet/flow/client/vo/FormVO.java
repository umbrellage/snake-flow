package com.juliet.flow.client.vo;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * FormVO
 *
 * @author Geweilang
 * @date 2023/5/10
 */
@Getter
@Setter
public class FormVO {

    private Long id;

    private String name;

    private String code;

    private String path;

    private List<FieldVO> fields;

}
