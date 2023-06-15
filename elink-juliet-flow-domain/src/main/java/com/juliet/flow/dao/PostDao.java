package com.juliet.flow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juliet.flow.domain.entity.FormEntity;
import com.juliet.flow.domain.entity.PostEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Mapper
public interface PostDao extends BaseMapper<PostEntity> {

    void insertBatch(List<PostEntity> postEntities);
}
