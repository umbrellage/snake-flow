package com.juliet.flow.domain.model;

import java.util.List;

/**
 * ActiveRule
 *
 * @author Geweilang
 * @date 2023/8/21
 */
public abstract class NotifyRule {

    public abstract String notifyRuleName();

    public abstract List<Long> notifyNodeIds();

    public abstract boolean activeSelf();

}
