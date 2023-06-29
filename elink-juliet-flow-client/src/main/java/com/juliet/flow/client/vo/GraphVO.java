package com.juliet.flow.client.vo;

import lombok.Data;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-06-19
 */
@Data
public class GraphVO {

    private List<GraphNodeVO> nodes;

    private List<GraphEdgeVO> edges;
}
