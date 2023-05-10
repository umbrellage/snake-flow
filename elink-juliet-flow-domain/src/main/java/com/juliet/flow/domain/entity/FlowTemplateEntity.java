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
@TableName(value = "juliet_flow_template")
public class FlowTemplateEntity extends BaseEntity {

    @TableId
    private Long id;

    @TableField(value = "template_name")
    private String name;

    private String code;

    private Integer status;
}
