package com.juliet.flow.client.dto;

import com.juliet.flow.client.common.ConditionTypeEnum;
import com.juliet.flow.client.common.JudgementTypeEnum;
import java.util.List;
import lombok.Data;

/**
 * AssignmentRuleDTO
 *
 * @author Geweilang
 * @date 2024/1/23
 */
@Data
public class AssignmentRuleDTO {

    private Long operatorUserId;
    private Long operatorUserName;
    private List<RuleDTO> rules;


    @Data
    public static class RuleDTO {
        private String fieldCode;
        private String fieldName;
        private JudgementTypeEnum  judgementType;
        private Long fieldValueId;
        private String fieldValueName;
        private ConditionTypeEnum conditionType;
    }



}
