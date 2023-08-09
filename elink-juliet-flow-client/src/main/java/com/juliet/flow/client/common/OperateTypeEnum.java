package com.juliet.flow.client.common;

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
}
