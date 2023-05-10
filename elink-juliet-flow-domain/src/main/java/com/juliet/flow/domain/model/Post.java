package com.juliet.flow.domain.model;

import lombok.Data;

/**
 *
 * 岗位信息
 *
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class Post extends BaseModel {

    private Long id;

    private String postId;

    private String postName;
}
