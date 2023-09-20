package com.juliet.flow.domain.model.rule.access;

import com.juliet.flow.domain.model.BaseRule;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * FobSpecialRule
 *
 * @author Geweilang
 * @date 2023/9/20
 */
@Component
public class FobSpecialRule extends BaseRule {

    @Override
    public String getRuleName() {
        return "fob_special";
    }

    @Override
    public boolean accessRule(Map<String, Object> params) {

        return true;
    }
}
