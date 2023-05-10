package com.juliet.flow.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xujianjie
 * @date 2023-04-27
 */
@TableName("product_item_price")
@Data
public class ItemPriceEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long itemId;

    private BigDecimal price;

    private Integer priceType;

    private String ext;

    private Integer delFlag;

    private Date createTime;

    private Date updateTime;

    private Long createBy;

    private Long updateBy;

    private Date repTime;
}
