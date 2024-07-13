package com.juliet.flow.repository;

import com.juliet.flow.client.dto.HistoricTaskQueryObject;
import com.juliet.flow.client.dto.HistoryTaskInstance;
import java.util.List;

/**
 * HistoryTaskRepository
 *
 * @author Geweilang
 * @date 2024/7/13
 */
public interface HistoryTaskRepository {

    List<HistoryTaskInstance> list(HistoricTaskQueryObject query);


}
