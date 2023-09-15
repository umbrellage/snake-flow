package com.juliet.flow.domain.model;

import java.util.List;
import java.util.Map;

/**
 * ActiveRule
 *
 * @author Geweilang
 * @date 2023/8/21
 */
public abstract class NotifyRule {

    public abstract String notifyRuleName();

    public abstract List<Long> notifyNodeIds(Flow flow, Map<String, Object> param);

    public abstract boolean activeSelf(Flow flow);

}
