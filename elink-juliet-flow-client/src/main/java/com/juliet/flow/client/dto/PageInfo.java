package com.juliet.flow.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageInfo<T> {

    private int pageNo = 1;

    //每次查询条数 默认10个
    private int pageSize = 10;

    //总记录数
    private int total;

    private List<T> items;
}
