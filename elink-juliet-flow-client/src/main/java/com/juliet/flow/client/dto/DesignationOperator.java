package com.juliet.flow.client.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * DesignationOperator
 *
 * @author Geweilang
 * @date 2024/4/12
 */
@Data
public class DesignationOperator {
    private Long flowId;
    private List<Long> nodeIdList = new ArrayList<>();
    private Long operator;
}
