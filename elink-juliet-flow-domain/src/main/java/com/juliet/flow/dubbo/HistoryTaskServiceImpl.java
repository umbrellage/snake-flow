package com.juliet.flow.dubbo;

import com.juliet.flow.client.HistoryTaskService;
import com.juliet.flow.client.dto.HistoricTaskInstanceQuery;
import com.juliet.flow.client.dto.HistoryTaskInstance;
import java.util.List;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * HistoryTaskServiceImpl
 *
 * @author Geweilang
 * @date 2024/7/12
 */
@Service
@DubboService(timeout = 5000)
public class HistoryTaskServiceImpl implements HistoryTaskService {

    @Override
    public List<HistoryTaskInstance> list(HistoricTaskInstanceQuery query) {
        return null;
    }
}
