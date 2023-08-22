package com.juliet.flow.service;

import com.juliet.flow.domain.dto.TaskForwardDTO;
import org.springframework.stereotype.Service;

/**
 * TaskService
 *
 * @author Geweilang
 * @date 2023/8/22
 */
public interface TaskService {


    void createSubFlowTask(TaskForwardDTO dto);

    void forwardMainFlowTask(TaskForwardDTO dto);

    void forwardSubFlowTask(TaskForwardDTO dto);
}
