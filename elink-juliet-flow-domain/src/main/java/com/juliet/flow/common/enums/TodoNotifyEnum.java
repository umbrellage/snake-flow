package com.juliet.flow.common.enums;

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
}
