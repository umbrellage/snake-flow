package com.juliet.flow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
@TableName("juliet_flow_node")
public class NodeEntity extends BaseEntity {

    private Long id;

    private Long parentId;

    private Long flowId;

    private Long flowTemplateId;

    @TableField(value = "node_status")
    private Integer status;

    private Integer nodeType;

    private Long processedBy;
}
