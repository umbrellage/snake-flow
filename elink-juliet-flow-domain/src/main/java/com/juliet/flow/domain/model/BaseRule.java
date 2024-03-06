package com.juliet.flow.domain.model;

import java.util.Map;

/**
 * @author xujianjie
 * @date 2023-05-08
 */
public abstract class BaseRule {

    public abstract String getRuleName();

    public abstract boolean accessRule(Map<String, Object> params, Long nodeId);
}
