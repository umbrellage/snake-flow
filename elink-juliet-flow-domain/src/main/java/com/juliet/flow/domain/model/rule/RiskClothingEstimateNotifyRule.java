package com.juliet.flow.domain.model.rule;

import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.domain.model.NotifyRule;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * RiskClothingEstimateNotifyRule
 *
 * @author Geweilang
 * @date 2024/3/12
 */
@Slf4j
@Component
public class RiskClothingEstimateNotifyRule extends NotifyRule {

    @Override
    public String notifyRuleName() {
        return "risk_clothing_estimate_pass";
    }

    @Override
    public List<Long> notifyNodeIds(Flow flow, Map<String, Object> param) {
        if (param == null) {
            log.info("param is null");
            return Collections.emptyList();
        }
        if (!param.containsKey("riskClothingEstimatePass")) {
            return Collections.emptyList();
        }
        Boolean complete = (Boolean) param.get("riskClothingEstimatePass");
        if (complete) {
            return flow.getNodes().stream()
                .filter(e -> StringUtils.equals(e.getModifyOtherTodoName(), notifyRuleName()))
                .map(Node::getId)
                .collect(Collectors.toList());

        }
        return Collections.emptyList();
    }

    @Override
    public boolean activeSelf(Flow flow) {
        return false;
    }

    @Override
    public boolean notifySelf(Flow flow, Map<String, Object> param) {
        return false;
    }
}
