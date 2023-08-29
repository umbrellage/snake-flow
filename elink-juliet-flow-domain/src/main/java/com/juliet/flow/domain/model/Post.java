package com.juliet.flow.domain.model;

import com.juliet.flow.client.vo.PostVO;
import com.juliet.flow.common.utils.IdGenerator;
import java.util.Date;
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
public class Post extends BaseModel {

    private Long id;

    private String postId;

    private String postName;

    public PostVO toPost() {
        PostVO data = new PostVO();
        BeanUtils.copyProperties(this, data);
        return data;
    }

    public Post deepCopy() {
        Post ret = new Post();
        ret.setId(IdGenerator.getId());
        ret.setPostId(postId);
        ret.setPostName(postName);
        ret.setTenantId(getTenantId());
        ret.setCreateBy(getCreateBy());
        ret.setUpdateBy(getUpdateBy());
        ret.setCreateTime(new Date());
        ret.setUpdateTime(new Date());
        return ret;
    }
}
