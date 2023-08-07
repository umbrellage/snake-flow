package com.juliet.flow.domain.model.rule;

import com.juliet.flow.domain.model.BaseAssignRule;
import com.juliet.flow.domain.model.Flow;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author xujianjie
 * @date 2023-06-14
 */
@Component
public class BrandAssignRule extends BaseAssignRule {

    @Override
    public String getRuleName() {
        return "assign_rule_brand";
    }

    @Override
    public Long getAssignUserId(Map<String, Object> params, Flow flow) {
        return null;
    }

    @Override
    public AssignSupplier getAssignSupplier(Map<String, Object> params, Flow flow) {
        return null;
    }
}
