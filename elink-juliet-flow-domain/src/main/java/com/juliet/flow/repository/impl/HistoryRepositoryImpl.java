package com.juliet.flow.repository.impl;

import com.juliet.flow.dao.HistoryDao;
import com.juliet.flow.domain.model.History;
import com.juliet.flow.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * HistoryRepositoryImpl
 *
 * @author Geweilang
 * @date 2023/8/4
 */
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


    private final HistoryDao historyDao;
}