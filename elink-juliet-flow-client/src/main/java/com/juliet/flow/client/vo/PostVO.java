package com.juliet.flow.client.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * PostVO
 *
 * @author Geweilang
 * @date 2023/5/10
 */
@Getter
@Setter
public class PostVO implements Serializable {

    private Long id;

    private String postId;

    private String postName;
}
