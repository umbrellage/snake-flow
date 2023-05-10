package com.juliet.flow.domain.dto;

import com.juliet.flow.client.dto.FieldDTO;
import lombok.Data;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class FlowOpenResultDTO {

    private List<FieldDTO> allowFields;
}
