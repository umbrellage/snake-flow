package com.juliet.flow.dubbo;

import com.juliet.flow.client.HistoryTaskService;
import com.juliet.flow.client.dto.HistoricTaskQueryObject;
import com.juliet.flow.client.dto.HistoryTaskInstance;
import com.juliet.flow.service.HistoryTaskInstanceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * HistoryTaskServiceImpl
 *
 * @author Geweilang
 * @date 2024/7/12
 */
@RequiredArgsConstructor
@Service
@DubboService(timeout = 5000)
public class HistoryTaskServiceImpl implements HistoryTaskService {

    @Override
    public List<HistoryTaskInstance> list(HistoricTaskQueryObject query) {
        return historyTaskInstanceService.list(query);
    }


    private final HistoryTaskInstanceService historyTaskInstanceService;
}
