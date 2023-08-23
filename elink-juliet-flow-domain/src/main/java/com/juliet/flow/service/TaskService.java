package com.juliet.flow.service;

import com.juliet.flow.client.dto.HistoricTaskInstance;
import com.juliet.flow.domain.dto.TaskForwardDTO;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * TaskService
 *
 * @author Geweilang
 * @date 2023/8/22
 */
public interface TaskService {


    List<HistoricTaskInstance> createSubFlowTask(TaskForwardDTO dto);

    List<HistoricTaskInstance> forwardMainFlowTask(TaskForwardDTO dto);

    List<HistoricTaskInstance> forwardSubFlowTask(TaskForwardDTO dto);
}
