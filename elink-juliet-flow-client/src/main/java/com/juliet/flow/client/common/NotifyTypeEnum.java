package com.juliet.flow.client.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * NotifyTypeEnum
 *
 * @author Geweilang
 * @date 2023/5/23
 */
@Getter
@AllArgsConstructor
public enum NotifyTypeEnum {

    /**
     *
     */
    SUPERVISOR_ASSIGNMENT("主管分配"),

    SELF_AND_SUPERVISOR_ASSIGNMENT("认领加调整"),

    DELETE("删除待办"),

    COMPLETE("完成待办"),

    CC("抄送"),

    ;

    private String msg;
}
