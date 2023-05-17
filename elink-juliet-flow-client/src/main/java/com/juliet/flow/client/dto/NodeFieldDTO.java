package com.juliet.flow.client.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * NodeFieldDTO
 *
 * @author Geweilang
 * @date 2023/5/16
 */
@Getter
@Setter
public class NodeFieldDTO {

    private List<String> fieldCodeList;
    private Long flowId;
}