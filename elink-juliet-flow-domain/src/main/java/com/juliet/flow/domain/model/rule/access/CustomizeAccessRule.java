package com.juliet.flow.domain.model.rule.access;

import com.alibaba.fastjson2.JSON;
import com.juliet.flow.client.common.ConditionTypeEnum;
import com.juliet.flow.client.common.CustomizeRuleEnum;
import com.juliet.flow.client.dto.AccessRuleDTO;
import com.juliet.flow.client.dto.RuleDTO;
import com.juliet.flow.common.utils.RuleUtil;
import com.juliet.flow.constant.FlowConstant;
import com.juliet.flow.domain.model.BaseRule;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * CustomizeAccessRule
 *
 * @author Geweilang
 * @date 2024/3/6
 */
@Slf4j
@Component
public class CustomizeAccessRule extends BaseRule {

    @Override
    public String getRuleName() {
        return CustomizeRuleEnum.ACCESS_RULE.getCode();
    }

    @Override
    public boolean accessRule(Map<String, Object> params, Long nodeId) {
        Flow flow = (Flow) params.get(FlowConstant.INNER_FLOW);
        if (flow == null) {
            return false;
        }
        Node node = flow.findNode(nodeId);
        List<AccessRuleDTO> ruleList = node.getAccessRuleList();
        boolean access = ruleList.stream().allMatch(e -> RuleUtil.matchRule(params, e.getRules()));
        log.info("CustomizeAssignRule access:{}, nodeId:{}, rule:{}, param:{}",
            access, nodeId, JSON.toJSONString(ruleList), JSON.toJSONString(params));
        return access;
    }
}
