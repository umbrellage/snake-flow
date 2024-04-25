package com.juliet.flow.client.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.vo.GraphEdgeVO.Property;
import java.io.Serializable;
import lombok.Data;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author xujianjie
 * @date 2023-06-19
 */
@Data
public class GraphVO implements Serializable {

    private List<GraphNodeVO> nodes;

    private List<GraphEdgeVO> edges;

    private Double x;
    private Double y;
    private Double zoom;

    public boolean canAlter() {
        if (CollectionUtils.isEmpty(nodes)) {
            return false;
        }
        long canClick = nodes.stream()
            .filter(node -> node.getProperties().isCanClick())
            .count();

        long canClickError = nodes.stream()
            .filter(node -> node.getProperties().isCanClickError())
            .count();

        return canClick > 0 || canClickError <= 0;
    }

}
