package com.juliet.flow.client.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
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

    private Text text;

    @Data
    public static class PointVO implements Serializable {

        private Integer x;

        private Integer y;
    }
    @Data
    public static class Property implements Serializable{
        @ApiModelProperty("true，通过线的，false 通过点的，特殊业务用与流程引擎无关")
        private boolean useEdgeStatus;

        private boolean activated;

        @ApiModelProperty("标准周期天数")
        private Integer standardCycleDays;

        @ApiModelProperty("实际周期天数")
        private Integer actualCycleDays;

        private String text;
    }
    @Data
    public static class Text implements Serializable {
        private String value;
        private Double x;
        private Double y;
    }

}
