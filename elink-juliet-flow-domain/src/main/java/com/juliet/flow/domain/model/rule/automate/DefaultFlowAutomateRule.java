package com.juliet.flow.domain.model.rule.automate;

import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowAutomateRule;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * DefaultFlowAutomateRule
 *
 * @author Geweilang
 * @date 2024/3/12
 */
@Component
public class DefaultFlowAutomateRule extends FlowAutomateRule {

    @Override
    public String flowAutomateRuleName() {
        return "flow_automate";
    }

    @Override
    public boolean flowAutomate(Flow flow, Map<String, Object> automateParam) {


        return false;
    }
}
