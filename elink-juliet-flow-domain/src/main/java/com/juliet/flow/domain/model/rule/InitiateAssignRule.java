package com.juliet.flow.domain.model.rule;

import com.juliet.flow.domain.model.BaseAssignRule;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.Node;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * InitiateAssignRule
 *
 * @author Geweilang
 * @date 2023/7/1
 */
@Component
public class InitiateAssignRule extends BaseAssignRule {

    @Override
    public String getRuleName() {
        return "assign_rule_initiate";
    }

    @Override
    public Long getAssignUserId(Map<String, Object> params, Flow flow) {
        if (flow == null) {
            return null;
        }
        Node node = flow.startNode();
        return node.getProcessedBy();
    }

    @Override
    public AssignSupplier getAssignSupplier(Map<String, Object> params, Flow flow) {
        return null;
    }
}
