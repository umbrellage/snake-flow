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

    /**
     * 需要通知的节点（可办转待办）
     * @param flow
     * @param param
     * @return
     */
    public abstract List<Long> notifyNodeIds(Flow flow, Map<String, Object> param);

    /**
     * 没有规则默认为true
     * @param flow
     * @return
     */
    public abstract boolean activeSelf(Flow flow);

    public abstract boolean notifySelf(Flow flow, Map<String, Object> param);

}
