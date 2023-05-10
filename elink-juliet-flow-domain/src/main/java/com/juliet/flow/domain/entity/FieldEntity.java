package com.juliet.flow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
@TableName("juliet_flow_node_form_field")
public class FieldEntity extends BaseEntity {

    @TableId
    private Long id;

    private Long formId;

    @TableField(value = "field_name")
    private String name;

    private String code;
}
