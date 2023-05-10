package com.juliet.flow.client.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttrFilter {

    /**
     * value优先，有value值时进行等于匹配
     */
    private Object value;

    /**
     * value不存在，存在list的情况下，进行多个值的或匹配
     */
    private List<Object> list;

    /**
     * 是否进行模糊查询，默认精确匹配
     */
    private boolean like;

    /**
     * 范围查找，大于等于
     */
    private Object greaterThanOrEqual;

    /**
     * 范围查找，小于等于
     */
    private Object lessThanOrEqual;
}
