package com.juliet.flow.domain.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class BaseEntity {

    private Integer delFlag;

    private Date createTime;

    private Date updateTime;

    private Long createBy;

    private Long updateBy;

    private Long tenantId;
}
