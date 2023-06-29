package com.juliet.flow.client.vo;

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

    private Text text;

    @Data
    public static class Properties {

        private String text;

        private String name;

        private boolean active;

        private boolean disabled;

        private boolean canClick;
    }

    @Data
    public static class Text {

        private Integer x;

        private Integer y;

        private String value;
    }
}
