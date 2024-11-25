package com.juliet.flow.domain.model.rule.notify;

import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.domain.model.NotifyRule;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * SampleReviewCompleted
 *
 * @author Geweilang
 * @date 2023/9/22
 */
@Slf4j
@Component
public class SampleReviewCompleted extends NotifyRule {

    @Override
    public String notifyRuleName() {
        return "sample_review_completed";
    }

    @Override
    public List<Long> notifyNodeIds(Flow flow, Map<String, Object> param) {
        if (param == null) {
            log.info("param is null");
            return Collections.emptyList();
        }
        if (param.containsKey("sampleReviewCompleted")) {
            Boolean complete = (Boolean) param.get("sampleReviewCompleted");
            if (complete) {
                return flow.getNodes().stream()
                        .filter(e -> StringUtils.equals(e.getModifyOtherTodoName(), notifyRuleName()))
                        .map(Node::getId)
                        .collect(Collectors.toList());

            }
        }
        return Collections.emptyList();
    }



    @Override
    public boolean activeSelf(Flow flow) {
        return true;
    }

    @Override
    public boolean notifySelf(Flow flow, Map<String, Object> param) {
        return false;
    }
}
