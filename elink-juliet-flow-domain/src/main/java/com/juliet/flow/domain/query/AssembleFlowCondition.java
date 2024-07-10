package com.juliet.flow.domain.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xujianjie
 * @date 2024-02-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssembleFlowCondition {

    private Boolean excludeFields;

    private Boolean excludePost;

    private Boolean excludeForm;

    private Boolean excludeSupplier;

    public static AssembleFlowCondition noExcludeFields() {
        AssembleFlowCondition condition = new AssembleFlowCondition();
        condition.setExcludeFields(false);
        return condition;
    }


}
