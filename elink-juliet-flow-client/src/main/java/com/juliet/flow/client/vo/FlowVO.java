package com.juliet.flow.client.vo;

import com.juliet.common.core.exception.ServiceException;
import java.util.List;
import java.util.stream.Collectors;
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

    private Boolean hasSubFlow;

    public List<NodeSimpleVO> nodeList() {
        if (hasSubFlow) {
            throw new ServiceException("当前流程已经存在一条异常流程");
        }
        return nodes.stream()
            .map(NodeVO::toSimple)
            .collect(Collectors.toList());
    }
}
