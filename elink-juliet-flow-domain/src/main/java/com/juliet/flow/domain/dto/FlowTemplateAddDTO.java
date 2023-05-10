package com.juliet.flow.domain.dto;

import com.juliet.flow.client.dto.NodeDTO;
import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class FlowTemplateAddDTO {

    private Long id;

    private String name;

    private String code;

    private NodeDTO node;
}
