package com.juliet.flow.domain.model;

import java.util.List;

/**
 * ActiveRule
 *
 * @author Geweilang
 * @date 2023/8/21
 */
public interface NotifyRule {

    String notifyRuleName();

    List<Long> notifyNodeIds();

}