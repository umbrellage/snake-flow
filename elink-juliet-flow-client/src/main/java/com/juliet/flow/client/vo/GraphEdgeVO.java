package com.juliet.flow.client.vo;

import lombok.Data;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-06-19
 */
@Data
public class GraphEdgeVO {

    private String id;

    private String type;

    private String sourceNodeId;

    private String targetNodeId;

    private PointVO startPoint;

    private PointVO endPoint;

    private List<PointVO> pointsList;

    @Data
    public static class PointVO {

        private Integer x;

        private Integer y;
    }
}
