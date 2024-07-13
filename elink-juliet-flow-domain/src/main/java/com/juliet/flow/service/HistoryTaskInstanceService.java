package com.juliet.flow.service;

import com.juliet.flow.client.dto.HistoricTaskQueryObject;
import com.juliet.flow.client.dto.HistoryTaskInstance;
import java.util.List;

/**
 * HistoryTaskInstanceService
 *
 * @author Geweilang
 * @date 2024/7/13
 */
public interface HistoryTaskInstanceService {


    List<HistoryTaskInstance> list(HistoricTaskQueryObject query);

}
