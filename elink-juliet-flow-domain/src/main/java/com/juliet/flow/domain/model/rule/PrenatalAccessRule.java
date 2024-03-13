package com.juliet.flow.domain.model.rule;

import com.juliet.flow.domain.model.BaseRule;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * PrenatalAccessRule
 *
 * @author Geweilang
 * @date 2023/8/7
 */
@Component
public class PrenatalAccessRule extends BaseRule {

    @Override
    public String getRuleName() {
        return "prenatal_unqualified_rule";
    }

    @Override
    public boolean accessRule(Map<String, Object> params, Long nodeId) {
        return !(params != null && params.containsKey(""));
    }
}
