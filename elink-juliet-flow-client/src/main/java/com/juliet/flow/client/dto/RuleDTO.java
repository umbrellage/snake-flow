package com.juliet.flow.client.dto;

import com.juliet.flow.client.common.ConditionTypeEnum;
import com.juliet.flow.client.common.JudgementTypeEnum;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * RuleDTO
 *
 * @author Geweilang
 * @date 2024/3/6
 */
@Data
public class RuleDTO {

    private String fieldCode;
    private String fieldName;
    private JudgementTypeEnum judgementType;
    private List<Selection<Object>> fieldValue;
    private ConditionTypeEnum conditionType;



    public boolean isMatch(Map<String, Object> params) {
        Object value = params.get(fieldCode);
        if (CollectionUtils.isEmpty(fieldValue)) {
            return false;
        }
        switch (judgementType) {
            case EQ:
                return eq(value);
            case not_eq:
                return notEq(value);
            case EQ_ANY:
                return eqAny(value);
            default:
                return false;
        }
    }

    private boolean eq(Object value) {
        return Objects.equals(String.valueOf(value), String.valueOf(fieldValue.get(0).getValue()));
    }

    private boolean notEq(Object value) {
        return !eq(value);
    }

    private boolean eqAny(Object value) {
        return fieldValue.stream()
            .map(Selection::getValue)
            .anyMatch(e -> Objects.equals(value, e));
    }
}
