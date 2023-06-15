package com.juliet.flow.domain.model;

import lombok.Data;

import java.util.Date;

/**
 * @author xujianjie
 * @date 2023-05-10
 */
@Data
public class BaseModel {

    private Long tenantId;

    private Long createBy;

    private Long updateBy;

    private Date createTime;

    private Date updateTime;
}
