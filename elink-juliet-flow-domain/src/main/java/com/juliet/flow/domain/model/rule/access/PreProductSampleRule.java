package com.juliet.flow.domain.model.rule.access;

import com.juliet.flow.domain.model.BaseRule;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * PreProductSampleRule
 *
 * @author Geweilang
 * @date 2023/8/9
 */
@Component
public class PreProductSampleRule extends BaseRule {

    @Override
    public String getRuleName() {
        return "pre_production_sample";
    }

    @Override
    public boolean accessRule(Map<String, Object> params) {
        Boolean value = (Boolean) params.get("isEnd");
        return value == null || !value;
    }
}
