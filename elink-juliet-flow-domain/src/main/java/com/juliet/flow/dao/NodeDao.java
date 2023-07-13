package com.juliet.flow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juliet.flow.domain.entity.FlowEntity;
import com.juliet.flow.domain.entity.NodeEntity;
import com.juliet.flow.domain.model.NodeQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Mapper
public interface NodeDao extends BaseMapper<NodeEntity> {

    List<NodeEntity> listNode(@Param("query") NodeQuery query);

    void insertBatch(@Param("nodeEntities") List<NodeEntity> nodeEntities);

}
