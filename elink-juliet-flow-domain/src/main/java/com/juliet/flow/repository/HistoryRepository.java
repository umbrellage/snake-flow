package com.juliet.flow.repository;

import com.juliet.flow.domain.model.History;
import java.util.List;

/**
 * HistoryRepository
 *
 * @author Geweilang
 * @date 2023/8/4
 */
public interface HistoryRepository {

    void add(History dto);

    void add(List<History> dto);

    List<History> queryByFlowId(Long flowId);
}
