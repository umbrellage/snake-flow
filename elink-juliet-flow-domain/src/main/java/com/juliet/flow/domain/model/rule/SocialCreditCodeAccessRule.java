package com.juliet.flow.domain.model.rule;

import com.juliet.flow.domain.model.BaseRule;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * SocialCreditCodeAccessRule
 *
 * @author Geweilang
 * @date 2023/7/25
 */
@Component
public class SocialCreditCodeAccessRule extends BaseRule {

    @Override
    public String getRuleName() {
        return "social_credit_code";
    }

    @Override
    public boolean accessRule(Map<String, Object> params) {
        Object value = params.get("isChange");
        return value != null;
    }
}
