package com.juliet.flow.client.dto;

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

    private List<Selection<Long>> operatorUser;
    private List<RuleDTO> rules;

}
