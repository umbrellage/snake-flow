package com.juliet.flow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juliet.flow.domain.entity.PostEntity;
import com.juliet.flow.domain.entity.SupplierEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Mapper
public interface SupplierDao extends BaseMapper<SupplierEntity> {

    void insertBatch(List<SupplierEntity> supplierEntities);
}
