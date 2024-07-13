package com.juliet.flow.service.impl;

import com.juliet.flow.client.dto.HistoricTaskQueryObject;
import com.juliet.flow.client.dto.HistoryTaskInstance;
import com.juliet.flow.repository.HistoryTaskRepository;
import com.juliet.flow.service.HistoryTaskInstanceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * HistoryTaskInstanceServiceImpl
 *
 * @author Geweilang
 * @date 2024/7/13
 */
@RequiredArgsConstructor
@Service
public class HistoryTaskInstanceServiceImpl implements HistoryTaskInstanceService {

    @Override
    public List<HistoryTaskInstance> list(HistoricTaskQueryObject query) {
        return historyTaskRepository.list(query);
    }



    private final HistoryTaskRepository historyTaskRepository;
}
