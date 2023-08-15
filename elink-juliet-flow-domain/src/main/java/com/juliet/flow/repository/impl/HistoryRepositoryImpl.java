package com.juliet.flow.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.juliet.flow.dao.HistoryDao;
import com.juliet.flow.domain.entity.HistoryEntity;
import com.juliet.flow.domain.model.History;
import com.juliet.flow.repository.HistoryRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * HistoryRepositoryImpl
 *
 * @author Geweilang
 * @date 2023/8/4
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryRepositoryImpl implements HistoryRepository {

    @Override
    public void add(History dto) {
        if (dto == null) {
            return;
        }
        historyDao.insert(dto.to());
    }

    @Override
    public void add(List<History> dto) {
        if (CollectionUtils.isNotEmpty(dto)) {
            dto.forEach(history -> historyDao.insert(history.to()));
        }
    }

    @Override
    public List<History> queryByFlowId(Long flowId) {
        if (flowId == null) {
            log.error("query history error, flowId is null");
            return Collections.emptyList();
        }
        List<HistoryEntity> historyEntityList = historyDao.selectList(
            Wrappers.<HistoryEntity>lambdaQuery().eq(HistoryEntity::getFlowId, flowId));

        return historyEntityList.stream()
            .map(History::of)
            .collect(Collectors.toList());
    }


    private final HistoryDao historyDao;
}