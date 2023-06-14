package com.juliet.flow.domain.model;

import java.util.Map;

/**
 * @author xujianjie
 * @date 2023-06-14
 */
public abstract class BaseAssignRule {

    public abstract String getRuleName();

    public abstract Long getAssignUserId(Map<String, Object> params);
}
