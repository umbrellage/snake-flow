package com.juliet.flow.domain.model;

import java.util.Collection;
import java.util.Collections;
import lombok.Data;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-17
 */
@Data
public class NodeQuery {

    private final static int DEFAULT_PAGE_SIZE = 20;

    private final static int MAX_PAGE_SIZE = 1000;

    private Long userId;

    private List<String> postIds;

    private Long tenantId;

    private Integer pageNo;

    private Integer pageSize;

    public Integer getOffset() {
        if (pageNo == null) {
            pageNo = 1;
        }
        return (pageNo - 1) * getPageSize();
    }

    public Integer getPageSize() {
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    public static NodeQuery findByUserId(Long userId) {
        NodeQuery data = new NodeQuery();
        data.setUserId(userId);
        return data;
    }

    public static NodeQuery findByPostId(Long postId) {
        NodeQuery data = new NodeQuery();
        if (postId != null) {
            String value = String.valueOf(postId);
            data.setPostIds(Collections.singletonList(value));
        }
        return data;
    }
}
