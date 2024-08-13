package com.juliet.flow.client.dto;

import java.util.List;
import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-05-10
 */
@Data
public class PostDTO {

    private String id;

    /**
     * 岗位ID
     */
    private String postId;

    /**
     * 岗位名称
     */
    private String postName;
}
