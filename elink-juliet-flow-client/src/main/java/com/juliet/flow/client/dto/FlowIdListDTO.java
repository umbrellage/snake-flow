package com.juliet.flow.client.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * FlowIdListDTO
 *
 * @author Geweilang
 * @date 2023/5/23
 */
@Getter
@Setter
public class FlowIdListDTO {

    private List<Long> flowIdList;

    private Boolean excludeFields;

}
