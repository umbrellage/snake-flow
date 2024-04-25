package com.juliet.flow.client.vo;

import java.io.Serializable;
import lombok.Data;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-06-19
 */
@Data
public class GraphEdgeVO implements Serializable {

    private String id;

    private String type;

    private String sourceNodeId;

    private String targetNodeId;

    private PointVO startPoint;

    private PointVO endPoint;

    private Property properties;

    private List<PointVO> pointsList;

    @Data
    public static class PointVO implements Serializable {

        private Integer x;

        private Integer y;
    }
    @Data
    public static class Property implements Serializable{
        private boolean activated;
    }

}
