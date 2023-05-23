package com.juliet.flow.client.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * NodeSimpleVO
 *
 * @author Geweilang
 * @date 2023/5/23
 */
@Getter
@Setter
public class NodeSimpleVO {


    private Long id;
    private String name;
    private Long processedBy;
    private Long flowId;
}
