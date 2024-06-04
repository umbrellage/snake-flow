package com.juliet.flow.client.common;

import cn.snake.kobe.enums.EnumWebService;
import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;

/**
 * JudgementTypeEnum
 *
 * @author Geweilang
 * @date 2024/1/23
 */
@Getter
public enum JudgementTypeEnum implements EnumWebService<Integer, String> {

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

    public static JudgementTypeEnum of(Integer code) {
        return Arrays.stream(values())
            .filter(e -> Objects.equals(e.getCode(), code))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("未找到对应的枚举值"));
    }

    @Override
    public Integer getEnumKey() {
        return code;
    }

    @Override
    public String getEnumValue() {
        return name;
    }
}
