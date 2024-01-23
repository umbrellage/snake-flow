package com.juliet.flow.client.common;

import lombok.Getter;

/**
 * ConditionTypeEnum
 *
 * @author Geweilang
 * @date 2024/1/23
 */
@Getter
public enum ConditionTypeEnum {

    OR(1, "或"),
    AND(2, "且"),
    ;


    private  final Integer code;
    private final String name;


    ConditionTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
