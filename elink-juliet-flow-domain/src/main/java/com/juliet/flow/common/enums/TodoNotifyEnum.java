package com.juliet.flow.common.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * TodoNotifyEnum
 *
 * @author Geweilang
 * @date 2023/8/21
 */
@Getter
public enum TodoNotifyEnum {

    /**
     *
     */
    NOTIFY(1),
    NO_NOTIFY(0),

    ;

    private final Integer code;

    TodoNotifyEnum(Integer code) {
        this.code = code;
    }

    public static TodoNotifyEnum of(Integer code) {
        return Arrays.stream(TodoNotifyEnum.values())
            .filter(e -> e.getCode().equals(code)).findAny()
            .orElse(null);
    }
}
