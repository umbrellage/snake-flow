package com.juliet.flow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
@TableName("juliet_flow_node_post")
public class PostEntity extends BaseEntity {

    @TableId
    private Long id;

    private Long nodeId;

    private String postId;

    private String postName;
}
