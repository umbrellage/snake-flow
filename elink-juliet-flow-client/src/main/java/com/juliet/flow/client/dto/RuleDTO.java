package com.juliet.flow.client.dto;

import com.juliet.flow.client.common.ConditionTypeEnum;
import com.juliet.flow.client.common.JudgementTypeEnum;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

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



    public boolean isMatch(Map<String, Object> params) {
        Object value = params.get(fieldCode);
        if (CollectionUtils.isEmpty(fieldValue)) {
            return false;
        }
        // TODO: 2024/3/6 JudgementTypeEnum 如果有其他类型，比如包含 需要修改
        boolean result = Objects.equals(String.valueOf(value), String.valueOf(fieldValue.get(0).getValue()));
        if (judgementType == JudgementTypeEnum.EQ) {
            return result;
        } else if(judgementType == JudgementTypeEnum.not_eq){
            return !result;
        }
        return false;
    }
}
