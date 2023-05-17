package com.juliet.flow.domain.model;

import lombok.Data;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-17
 */
@Data
public class NodeQuery {

    private Long userId;

    private List<String> postIds;

    private Long tenantId;

    private Integer pageNo;

    private Integer pageSize;
}
