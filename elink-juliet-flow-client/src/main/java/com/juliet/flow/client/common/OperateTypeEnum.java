package com.juliet.flow.client.common;

import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;

/**
 * OperateTypeEnum
 *
 * @author Geweilang
 * @date 2023/8/3
 */
@Getter
public enum OperateTypeEnum {
    /**
     * 回退
     */
    ROLLBACK(1),

    REJECT(2),

    ;


    private final Integer code;

    OperateTypeEnum(Integer code) {
        this.code = code;
    }

    public static OperateTypeEnum of(Integer code) {
        return Arrays.stream(OperateTypeEnum.values())
            .filter(type -> Objects.equals(type.code, code))
            .findAny()
            .orElse(null);
    }
}
