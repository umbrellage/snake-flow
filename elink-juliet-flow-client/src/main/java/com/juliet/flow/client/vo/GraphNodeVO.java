package com.juliet.flow.client.vo;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-06-19
 */
@Data
public class GraphNodeVO implements Serializable {

    private String id;

    /**
     * start
     * rect
     * end
     */
    private String type;

    @Deprecated
    // 1,品牌节点，2， 供应商节点
    private Integer nodeCategory;

    private Integer x;

    private Integer y;

    private Properties properties;

    private Text text;

    @Data
    public static class Properties implements Serializable{

        private String text;

        private String name;
        /**
         * true隐藏可办待办
         */
        private boolean hideTodoType;

        /**
         * 是否是当前节点
         */
        private boolean activated;
        /**
         * 是否是待办节点
         */
        private Boolean required;

        /**
         * 是否已完成
         */
        private Boolean finished;
        /**
         * 是否禁用
         */
        private Boolean disabled;
        /**
         * 可以变更
         */
        private boolean canClick;

        private boolean canClickError;

        private String clickRemark;

        private boolean canAdjustment;

        private String nodeId;

        private boolean canEdit;

        // 1,品牌节点，2， 供应商节点
        private Integer nodeCategory;

        private String currentProcessUserId;

        private String currentProcessUserName;

        private Long processBy;

        private String operateTime;

        private List<PostVO> bindPost;

        private String processByPostId;

        private String processByPostName;
    }

    @Data
    public static class Text implements Serializable {

        private Integer x;

        private Integer y;

        private String value;
    }
}
