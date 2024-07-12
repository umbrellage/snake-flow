package com.juliet.flow.client.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * HistoricTaskInstanceQuery
 *
 * @author Geweilang
 * @date 2024/7/12
 */
public interface HistoricTaskInstanceQuery  {

    HistoricTaskInstanceQuery processInstanceIds(Set<Long> processInstanceIds);

    HistoricTaskInstanceQuery processInstanceId(Long processInstanceId);

    HistoricTaskInstanceQuery taskAssignee(Long assignee);

    HistoricTaskInstanceQuery taskBpmId(Long bpmId);

    HistoricTaskInstanceQuery finished();

    HistoricTaskInstanceQuery finishedBefore(LocalDateTime time);

    HistoricTaskInstanceQuery finishedAfter(LocalDateTime time);









    static HistoricTaskInstanceQuery createHistoricTaskInstanceQuery() {
        return new HistoricTaskQueryObject();
    }

}
