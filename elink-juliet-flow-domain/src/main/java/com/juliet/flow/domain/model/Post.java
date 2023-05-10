package com.juliet.flow.domain.model;

import com.juliet.flow.client.vo.PostVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 *
 * 岗位信息
 *
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class Post {

    private Long id;

    private String postId;

    private String postName;

    public PostVO toPost() {
        PostVO data = new PostVO();
        BeanUtils.copyProperties(this, data);
        return data;
    }
}
