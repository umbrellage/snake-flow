package com.juliet.flow.domain.query;

import lombok.Data;

/**
 * @author xujianjie
 * @date 2024-02-28
 */
@Data
public class AssembleFlowCondition {

    private Boolean excludeFields;


    public static AssembleFlowCondition noExcludeFields() {
        AssembleFlowCondition condition = new AssembleFlowCondition();
        condition.setExcludeFields(false);
        return condition;
    }
}
