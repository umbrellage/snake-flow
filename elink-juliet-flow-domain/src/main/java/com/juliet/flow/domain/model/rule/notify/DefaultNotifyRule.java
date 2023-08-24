package com.juliet.flow.domain.model.rule.notify;

import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.domain.model.NotifyRule;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * DefaultNotifyRule
 *
 * @author Geweilang
 * @date 2023/8/23
 */
@Component
public class DefaultNotifyRule extends NotifyRule {

    @Override
    public String notifyRuleName() {
        return "special_process_active_notify";
    }

    @Override
    public List<Long> notifyNodeIds(Flow flow) {
        boolean handled = flow.getNodes().stream()
            .filter(node -> StringUtils.equalsAny(node.getName(), "c", "d"))
            .allMatch(node -> node.getStatus() == NodeStatusEnum.PROCESSED);
        if (handled) {
            List<Long> idList = flow.getNodes().stream()
                .filter(node -> StringUtils.equals(node.getName(), "g"))
                .map(Node::getId)
                .collect(Collectors.toList());
            return idList;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean activeSelf(Flow flow) {

        return flow.getNodes().stream()
            .filter(node -> StringUtils.equalsAny(node.getName(), "c", "d", "g"))
            .allMatch(node -> node.getStatus() == NodeStatusEnum.PROCESSED);
    }
}
