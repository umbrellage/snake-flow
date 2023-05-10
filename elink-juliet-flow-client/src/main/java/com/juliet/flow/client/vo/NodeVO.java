package com.juliet.flow.client.vo;

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

    private Long flowId;

    /**
     * 表单
     */
    private FormVO form;

    private List<PostVO> bindPosts;
    /**
     * 处理人
     */
    private Long processedBy;
}
