package com.juliet.flow.domain.dto;

import com.juliet.flow.client.dto.FieldDTO;
import lombok.Data;

import java.util.List;

/**
 * @author xujianjie
 * @date 2024-11-07
 */
@Data
public class FlowNodeSupervisorUpdateDTO {

    private Long flowTemplateId;

    private Long flowId;

    private String nodeTitle;

    private List<Long> supervisorIdList;
}
