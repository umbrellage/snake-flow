package com.juliet.flow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
@TableName("juliet_flow_node_form")
public class FormEntity extends BaseEntity {

    private Long id;

    private Long nodeId;

    @TableField(value = "form_name")
    private String name;

    private String code;

    private String path;

    private Integer status;
}
