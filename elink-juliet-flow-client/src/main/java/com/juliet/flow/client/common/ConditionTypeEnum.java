package com.juliet.flow.client.common;

import java.util.Arrays;
import java.util.Objects;
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

    public static ConditionTypeEnum of(Integer code) {
        return Arrays.stream(values())
            .filter(e -> Objects.equals(e.getCode(), code))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("未找到对应的枚举值"));
    }

}
