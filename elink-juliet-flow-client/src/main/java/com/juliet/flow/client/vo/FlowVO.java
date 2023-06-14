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
    /**
     * 异常流程数量
     */
    private Integer subFlowCount;

    /**
     * IN_PROGRESS(1, "进行中"),
     * ABNORMAL(2, "异常中"),
     * END(3, "已结束"),
     */
    private Integer status;


    public List<Long> processedBy() {
        return nodes.stream()
            .filter(nodeVO -> nodeVO.getStatus() == 3)
            .map(NodeVO::getProcessedBy)
            .distinct()
            .collect(Collectors.toList());
    }


    public List<NodeSimpleVO> nodeList() {

        return nodes.stream()
            .map(NodeVO::toSimple)
            .collect(Collectors.toList());
    }

}
