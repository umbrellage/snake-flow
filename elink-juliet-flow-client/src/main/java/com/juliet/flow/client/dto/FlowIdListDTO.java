package com.juliet.flow.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * FlowIdListDTO
 *
 * @author Geweilang
 * @date 2023/5/23
 */
@Getter
@Setter
public class FlowIdListDTO implements Serializable {

    private List<Long> flowIdList;

    private Boolean excludeFields;

}
