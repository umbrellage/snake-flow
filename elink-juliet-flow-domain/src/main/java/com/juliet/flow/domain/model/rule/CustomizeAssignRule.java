package com.juliet.flow.domain.model.rule;

import com.juliet.flow.domain.model.BaseAssignRule;
import com.juliet.flow.domain.model.Flow;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * CustomizeAssignRule
 *
 * @author Geweilang
 * @date 2024/1/24
 */
@Component
public class CustomizeAssignRule extends BaseAssignRule {

    @Override
    public String getRuleName() {
        return "assignment_rule";
    }

    @Override
    public Long getAssignUserId(Map<String, Object> params, Flow flow) {



        return null;
    }
}
