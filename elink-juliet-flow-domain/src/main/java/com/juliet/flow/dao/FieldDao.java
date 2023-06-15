package com.juliet.flow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juliet.flow.domain.entity.FieldEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Mapper
public interface FieldDao extends BaseMapper<FieldEntity> {

    void insertBatch(List<FieldEntity> fieldEntities);
}
