package com.juliet.flow.client.common;

import lombok.Getter;

/**
 * JudgementTypeEnum
 *
 * @author Geweilang
 * @date 2024/1/23
 */
@Getter
public enum JudgementTypeEnum {

    EQ(0, "等于"),

    not_eq(1, "不等于"),

//    lt(2,"小于"),
//
//    gt(3,"大于"),
    ;


    private final Integer code;
    private final String name;

    JudgementTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
