package com.juliet.flow.common.utils;

import com.juliet.flow.client.common.ConditionTypeEnum;
import com.juliet.flow.client.dto.RuleDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;

/**
 * RuleUtil
 *
 * @author Geweilang
 * @date 2024/3/7
 */
@NoArgsConstructor
public final class RuleUtil {

    public static boolean matchRule(Map<String, Object> params, List<RuleDTO> rules) {

        List<List<RuleDTO>> ruleGroupList = new ArrayList<>();
        List<RuleDTO> ruleDTOList = new ArrayList<>();
        for (RuleDTO ruleDTO : rules) {
            if (ruleDTO.getConditionType() == ConditionTypeEnum.OR || ruleDTO.getConditionType() == null) {
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
