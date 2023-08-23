package com.juliet.flow.domain.model.rule.notify;

import com.juliet.flow.domain.model.NotifyRule;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * DefaultNotifyRule
 *
 * @author Geweilang
 * @date 2023/8/23
 */
@Component
public class DefaultNotifyRule extends NotifyRule {

    @Override
    public String notifyRuleName() {
        return null;
    }

    @Override
    public List<Long> notifyNodeIds() {
        return Collections.emptyList();
    }

    @Override
    public boolean activeSelf() {
        return false;
    }
}
