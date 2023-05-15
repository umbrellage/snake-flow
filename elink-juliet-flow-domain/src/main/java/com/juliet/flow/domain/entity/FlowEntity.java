package com.juliet.flow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
@TableName(value = "jbpm_flow")
public class FlowEntity extends BaseEntity {

    @TableId
    private Long id;

    @TableField(value = "flow_name")
    private String name;

    private Long parentId;

    private Long flowTemplateId;

    private Integer status;
}
