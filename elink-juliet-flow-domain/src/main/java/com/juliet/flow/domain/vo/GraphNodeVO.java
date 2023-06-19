package com.juliet.flow.domain.vo;

import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-06-19
 */
@Data
public class GraphNodeVO {

    private String id;

    /**
     * start
     * rect
     * end
     */
    private String type;

    private Integer x;

    private Integer y;

    private Properties properties;

    @Data
    public static class Properties {

        private String text;

        private String name;

        private boolean active;

        private boolean disabled;
    }

    @Data
    public static class Text {

        private Integer x;

        private Integer y;

        private String value;
    }
}
