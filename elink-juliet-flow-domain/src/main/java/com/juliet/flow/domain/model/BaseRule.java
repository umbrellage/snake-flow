package com.juliet.flow.domain.model;

/**
 * @author xujianjie
 * @date 2023-05-08
 */
public abstract class BaseRule {

    public abstract boolean fire(Node node);
}
