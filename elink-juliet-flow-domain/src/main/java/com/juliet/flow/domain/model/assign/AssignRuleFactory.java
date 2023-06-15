package com.juliet.flow.domain.model.assign;

import com.juliet.flow.domain.model.BaseAssignRule;
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
public class AssignRuleFactory {

    private static List<BaseAssignRule> rules;

    @Autowired
    private void setRules(List<BaseAssignRule> rules) {
        AssignRuleFactory.rules = rules;
    }


    public static BaseAssignRule getAssignRule(String name) {
        if (name == null) {
            log.error("getAssignRule, name is null!");
            return null;
        }
        if (CollectionUtils.isEmpty(rules)) {
            log.warn("can not find system assign rule!");
            return null;
        }
        for (BaseAssignRule rule : rules) {
            if (name.equals(rule.getRuleName())) {
                return rule;
            }
        }
        return null;
    }
}
