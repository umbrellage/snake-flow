package com.juliet.flow.client.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class NodeDTO {

    private String id;

    private String title;

    @NotNull
    private String name;

    @NotNull
    private FormDTO form;

    @NotNull
    private String preName;

    @NotNull
    private String nextName;

    /**
     * 值参考NodeTypeEnum
     */
    @NotNull
    private Integer type;

    private List<PostDTO> bindPosts;

    @NotNull
    private Integer status;
}
