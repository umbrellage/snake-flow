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
        // TODO 分配规则 辅料开发账号
        return 13266131907L;
    }
}
