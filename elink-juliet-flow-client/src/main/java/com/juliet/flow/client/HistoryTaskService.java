package com.juliet.flow.client;

import com.juliet.flow.client.dto.HistoricTaskInstanceQuery;
import com.juliet.flow.client.dto.HistoryTaskInstance;
import java.util.List;

/**
 * TaskService
 *
 * @author Geweilang
 * @date 2024/7/12
 */
public interface HistoryTaskService {


    List<HistoryTaskInstance> list(HistoricTaskInstanceQuery query);

}
