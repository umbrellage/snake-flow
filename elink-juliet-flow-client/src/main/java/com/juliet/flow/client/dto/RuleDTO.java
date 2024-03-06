package com.juliet.flow.client.dto;

import com.juliet.flow.client.common.ConditionTypeEnum;
import com.juliet.flow.client.common.JudgementTypeEnum;
import java.util.List;
import lombok.Data;

/**
 * RuleDTO
 *
 * @author Geweilang
 * @date 2024/3/6
 */
@Data
public class RuleDTO {

    private String fieldCode;
    private String fieldName;
    private JudgementTypeEnum judgementType;
    private List<Selection<Object>> fieldValue;
    private ConditionTypeEnum conditionType;
}
