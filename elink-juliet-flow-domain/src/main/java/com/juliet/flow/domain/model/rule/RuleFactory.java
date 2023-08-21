package com.juliet.flow.domain.model.rule;

import com.juliet.flow.domain.model.ActiveRule;
import com.juliet.flow.domain.model.BaseAssignRule;
import com.juliet.flow.domain.model.BaseRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-06-15
 */
@Component
@Slf4j
public class RuleFactory {

    private static List<BaseAssignRule> assignRules;

    private static List<BaseRule> accessRules;

    private static List<ActiveRule> activeRules;

    @Autowired
    private void setAssignRules(List<BaseAssignRule> rules) {
        RuleFactory.assignRules = rules;
    }

    @Autowired
    private void setAccessRules(List<BaseRule> rules) {
        RuleFactory.accessRules = rules;
    }

    @Autowired
    private void setActiveRule(List<ActiveRule> rules) {
        RuleFactory.activeRules = rules;
    }

    public static ActiveRule activeRule(String name) {
        if (name == null) {
            log.error("activeRule, name is null!");
            return null;
        }
        if (CollectionUtils.isEmpty(activeRules)) {
            log.warn("can not find system active rule!");
            return null;
        }
        for (ActiveRule rule : activeRules) {
            if (name.equals(rule.activeRuleName())) {
                return rule;
            }
        }
        return null;
    }


    public static BaseAssignRule getAssignRule(String name) {
        if (name == null) {
            log.error("getAssignRule, name is null!");
            return null;
        }
        if (CollectionUtils.isEmpty(assignRules)) {
            log.warn("can not find system assign rule!");
            return null;
        }
        for (BaseAssignRule rule : assignRules) {
            if (name.equals(rule.getRuleName())) {
                return rule;
            }
        }
        return null;
    }

    public static BaseRule getAccessRule(String name) {
        if (name == null) {
            log.error("getAccessRule, name is null!");
            return null;
        }
        if (CollectionUtils.isEmpty(accessRules)) {
            log.warn("can not find system access rule!");
            return null;
        }
        for (BaseRule rule : accessRules) {
            if (name.equals(rule.getRuleName())) {
                return rule;
            }
        }
        return null;
    }
}
