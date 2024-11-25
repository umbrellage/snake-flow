package com.juliet.flow.domain.model.rule;

import com.juliet.flow.domain.model.BaseRule;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * SocialCreditCodeAccessRule
 *
 * @author Geweilang
 * @date 2023/7/25
 */
@Component
public class SocialCreditCodeNotChangeAccessRule extends BaseRule {

    @Override
    public String getRuleName() {
        return "social_credit_code_not_change";
    }

    @Override
    public boolean accessRule(Map<String, Object> params, Long nodeId) {
        return params == null || !params.containsKey("isChange");
    }
}
