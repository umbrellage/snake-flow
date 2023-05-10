package com.juliet.flow.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author xujianjie
 * @date 2023-04-27
 */
@Data
@TableName("product_item_attribute")
public class ItemAttributeEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long itemId;

    private String attrName;

    private String attrValue;

    private Integer filterFlag;

    private String ext;

    private Integer delFlag;

    private Date createTime;

    private Date updateTime;

    private Long createBy;

    private Long updateBy;

    private Date repTime;
}
