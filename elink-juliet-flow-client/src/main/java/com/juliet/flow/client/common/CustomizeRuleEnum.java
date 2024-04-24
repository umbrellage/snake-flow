package com.juliet.flow.client.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CustomizeRuleEnum
 *
 * @author Geweilang
 * @date 2024/4/24
 */
@Getter
@AllArgsConstructor
public enum CustomizeRuleEnum {

    ACCESS_RULE("customize_access_rule", "可入规则名称"),

    CUSTOMIZE_FLOW_AUTOMATE("customize_flow_automate", "自动流转规则"),

    CUSTOMIZE_ASSIGN("assignment_rule", "自动分配规则"),

    ;



    private final String code;
    private final String name;
}
