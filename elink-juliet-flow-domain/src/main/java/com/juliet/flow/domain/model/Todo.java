package com.juliet.flow.domain.model;

import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Data
public class Todo {

    private Long id;

    private String title;

    private String msg;

    private String path;

    /**
     * 处理人的ID
     */
    private String userId;

    /**
     * 处理人名字
     */
    private String userName;
}
