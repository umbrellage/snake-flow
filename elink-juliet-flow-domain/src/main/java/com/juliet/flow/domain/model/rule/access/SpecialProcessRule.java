package com.juliet.flow.domain.model.rule.access;

import com.juliet.flow.domain.model.BaseRule;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * SpecialProcessRule
 *
 * @author Geweilang
 * @date 2023/8/24
 */
@Component
public class SpecialProcessRule extends BaseRule {

    @Override
    public String getRuleName() {
        return "special_process_access";
    }

    @Override
    public boolean accessRule(Map<String, Object> params) {
        return  (boolean) params.get("specialTec");
    }
}
