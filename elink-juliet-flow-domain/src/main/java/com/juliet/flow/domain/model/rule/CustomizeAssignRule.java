package com.juliet.flow.domain.model.rule;

import com.juliet.flow.client.common.ConditionTypeEnum;
import com.juliet.flow.client.common.CustomizeRuleEnum;
import com.juliet.flow.client.dto.AssignmentRuleDTO;
import com.juliet.flow.client.dto.RuleDTO;
import com.juliet.flow.client.dto.Selection;
import com.juliet.flow.common.utils.RuleUtil;
import com.juliet.flow.domain.model.BaseAssignRule;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        return CustomizeRuleEnum.CUSTOMIZE_ASSIGN.getCode();
    }

    @Override
    public Long getAssignUserId(Map<String, Object> params, Flow flow, Long nodeId) {
        if (flow == null) {
            return null;
        }
        Node node = flow.findNode(nodeId);
        List<AssignmentRuleDTO> ruleList = node.getRuleList();
        return ruleList.stream()
            .filter(e -> RuleUtil.matchRule(params, e.getRules()))
            .map(AssignmentRuleDTO::getOperatorUser)
            .flatMap(Collection::stream)
            .map(Selection::getValue)
            .findAny()
            .orElse(null);
    }

}
