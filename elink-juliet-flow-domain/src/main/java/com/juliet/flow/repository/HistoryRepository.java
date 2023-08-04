package com.juliet.flow.repository;

import com.juliet.flow.domain.model.History;

/**
 * HistoryRepository
 *
 * @author Geweilang
 * @date 2023/8/4
 */
public interface HistoryRepository {

    void add(History dto);
}
