package com.juliet.flow.domain.model.rule.operator;

import com.juliet.flow.domain.model.BaseAssignRule;
import com.juliet.flow.domain.model.Flow;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AutoOperatorRule extends BaseAssignRule {
    @Override
    public String getRuleName() {
        return "auto_operator";
    }

    @Override
    public Long getAssignUserId(Map<String, Object> params, Flow flow) {
        if (params == null || params.get("autoOperator") == null) {
            return null;
        }
        Object value = params.get("autoOperator");
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            return Long.valueOf(String.valueOf(params.get("autoOperator")));
        }
        return (Long) params.get("autoOperator");
    }
}
