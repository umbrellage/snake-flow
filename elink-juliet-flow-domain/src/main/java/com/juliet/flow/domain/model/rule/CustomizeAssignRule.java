package com.juliet.flow.domain.model.rule;

import com.juliet.flow.client.common.ConditionTypeEnum;
import com.juliet.flow.client.dto.AssignmentRuleDTO;
import com.juliet.flow.client.dto.RuleDTO;
import com.juliet.flow.client.dto.Selection;
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
        return "assignment_rule";
    }

    @Override
    public Long getAssignUserId(Map<String, Object> params, Flow flow, Long nodeId) {
        if (flow == null) {
            return null;
        }
        Node node = flow.findNode(nodeId);
        List<AssignmentRuleDTO> ruleList = node.getRuleList();
        return ruleList.stream()
            .filter(e -> matchRule(params, e.getRules()))
            .map(AssignmentRuleDTO::getOperatorUser)
            .flatMap(Collection::stream)
            .map(Selection::getValue)
            .findAny()
            .orElse(null);
    }



    private boolean matchRule(Map<String, Object> params, List<RuleDTO> rules) {

        List<List<RuleDTO>> ruleGroupList = new ArrayList<>();
        List<RuleDTO> ruleDTOList = new ArrayList<>();
        for (RuleDTO ruleDTO : rules) {
            if (ruleDTO.getConditionType() == ConditionTypeEnum.OR) {
                ruleDTOList.add(ruleDTO);
                ruleGroupList.add(ruleDTOList);
                ruleDTOList = new ArrayList<>();
            }
            if (ruleDTO.getConditionType() == ConditionTypeEnum.AND) {
                ruleDTOList.add(ruleDTO);
            }
        }

        return ruleGroupList.stream()
            .anyMatch(list -> list.stream().allMatch(rule -> rule.isMatch(params)));
    }


}
