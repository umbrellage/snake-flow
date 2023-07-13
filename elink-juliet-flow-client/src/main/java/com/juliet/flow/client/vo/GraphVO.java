package com.juliet.flow.client.vo;

import lombok.Data;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author xujianjie
 * @date 2023-06-19
 */
@Data
public class GraphVO {

    private List<GraphNodeVO> nodes;

    private List<GraphEdgeVO> edges;

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
