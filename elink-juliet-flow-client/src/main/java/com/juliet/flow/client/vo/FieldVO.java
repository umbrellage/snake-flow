package com.juliet.flow.client.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * FieldVo
 *
 * @author Geweilang
 * @date 2023/5/10
 */
@Getter
@Setter
public class FieldVO implements Serializable {

    private String id;

    private String code;

    private String name;
}
