package com.juliet.flow.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Data;

/**
 * ProcessConfigDTO
 *
 * @author Geweilang
 * @date 2024/1/11
 */
@Data
public class ProcessConfigRPCDTO {

    @ApiModelProperty("流程节点")
    private List<ProcessNode> nodes;
    private List<Edge> edges;
    @JsonProperty("X")
    private Double x;
    @JsonProperty("Y")
    private Double y;
    private Double zoom;

    @Data
    public static class ProcessNode {
        private String id;
        private String type;
        private Integer x;
        private Integer y;
        private Properties properties;
        private Text text;

    }

    @Data
    public static class Properties {
        private String text;
        private String name;
        @ApiModelProperty("分配模式")
        private List<Integer> distributeType;
        @ApiModelProperty("指定人员")
        private Boolean designatedPerson;
        @ApiModelProperty("审核模式")
        private Integer auditType;
        @ApiModelProperty("是否待办")
        private Integer todoType;

    }

    @Data
    public static class Text {
        private Integer x;
        private Integer y;
        private String value;
    }

    @Data
    public static class Edge {
        private String id;
        private String type;
        private String sourceNodeId;
        private String targetNodeId;
        private Point startPoint;
        private Point endPoint;
        private List<Point> pointsList;

    }

    @Data
    public static class Point {
        private Integer x;
        private Integer y;
    }


}
