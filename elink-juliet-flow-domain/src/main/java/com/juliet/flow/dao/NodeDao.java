package com.juliet.flow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juliet.flow.domain.entity.FlowEntity;
import com.juliet.flow.domain.entity.NodeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Mapper
public interface NodeDao extends BaseMapper<NodeEntity> {
}
