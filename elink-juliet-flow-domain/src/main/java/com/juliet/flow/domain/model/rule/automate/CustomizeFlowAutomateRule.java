package com.juliet.flow.domain.model.rule.automate;

import com.juliet.flow.client.common.CustomizeRuleEnum;
import com.juliet.flow.client.dto.AccessRuleDTO;
import com.juliet.flow.common.utils.RuleUtil;
import com.juliet.flow.constant.FlowConstant;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowAutomateRule;
import com.juliet.flow.domain.model.Node;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * CustomizeFlowAutomateRule
 *
 * @author Geweilang
 * @date 2024/4/23
 */
@Component
public class CustomizeFlowAutomateRule extends FlowAutomateRule {

    @Override
    public String flowAutomateRuleName() {
        return CustomizeRuleEnum.CUSTOMIZE_FLOW_AUTOMATE.getCode();
    }

    @Override
    public boolean flowAutomate(Node node, Map<String, Object> automateParam) {
        List<AccessRuleDTO> ruleList = node.getAccessRuleList();
        return ruleList.stream().allMatch(e -> RuleUtil.matchRule(automateParam, e.getRules()));
    }
}
