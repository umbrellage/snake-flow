package com.juliet.flow.client.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * NodeVO
 *
 * @author Geweilang
 * @date 2023/5/10
 */
@Getter
@Setter
public class NodeVO {

    private Long id;

    private String name;

    private String preName;

    private String nextName;

    private Integer status;

    private Long flowId;

    /**
     * 表单
     */
    private FormVO form;

    private List<PostVO> bindPosts;

    /**
     * 上一个处理人
     */
    private List<Long> preprocessedBy;
    /**
     * 处理人
     */
    private Long processedBy;
    private LocalDateTime processedTime;
}
