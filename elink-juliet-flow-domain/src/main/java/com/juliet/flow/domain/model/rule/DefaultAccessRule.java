package com.juliet.flow.domain.model.rule;

import com.juliet.flow.domain.model.BaseRule;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author xujianjie
 * @date 2023-06-15
 */
@Component
public class DefaultAccessRule extends BaseRule {

    @Override
    public String getRuleName() {
        return "access_rule_default";
    }

    @Override
    public boolean check(Map<String, Object> params) {
        return true;
    }
}
