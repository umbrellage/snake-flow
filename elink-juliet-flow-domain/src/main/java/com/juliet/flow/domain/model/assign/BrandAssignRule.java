package com.juliet.flow.domain.model.assign;

import com.juliet.flow.domain.model.BaseAssignRule;

import java.util.Map;

/**
 * @author xujianjie
 * @date 2023-06-14
 */
public class BrandAssignRule extends BaseAssignRule {

    @Override
    public String getRuleName() {
        return "assign_rule_brand";
    }

    @Override
    public Long getAssignUserId(Map<String, Object> params) {
        // TODO 分配规则
        return 123L;
    }
}
