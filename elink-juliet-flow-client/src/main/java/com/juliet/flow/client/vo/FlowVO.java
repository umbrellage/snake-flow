package com.juliet.flow.client.vo;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * FlowVO
 *
 * @author Geweilang
 * @date 2023/5/11
 */
@Getter
@Setter
public class FlowVO {
    private Long id;

    private String name;
    /**
     * 当流程为子流程时，存在父流程
     */
    private Long parentId;

    private Long flowTemplateId;

    private List<NodeVO> nodes;

    private Long tenantId;
}
