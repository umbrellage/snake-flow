package com.juliet.flow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juliet.flow.domain.entity.HistoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * HistoryDao
 *
 * @author Geweilang
 * @date 2023/8/4
 */
@Mapper
public interface HistoryDao extends BaseMapper<HistoryEntity> {

}
