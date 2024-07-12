package com.juliet.flow.client.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import lombok.Data;

/**
 * HistoricTaskQueryObject
 *
 * @author Geweilang
 * @date 2024/7/12
 */
@Data
public class HistoricTaskQueryObject implements HistoricTaskInstanceQuery, Serializable {

    private Long taskAssignee;
    private Long taskBpmId;
    private Boolean finished;
    private Long processInstanceId;
    private Collection<Long> processInstanceIds;
    private LocalDateTime finishedBefore;
    private LocalDateTime finishedAfter;


    @Override
    public HistoricTaskInstanceQuery processInstanceIds(Set<Long> processInstanceIds) {
        this.processInstanceIds = processInstanceIds;
        return this;
    }

    @Override
    public HistoricTaskInstanceQuery processInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    @Override
    public HistoricTaskInstanceQuery taskAssignee(Long assignee) {
        taskAssignee = assignee;
        return this;
    }

    @Override
    public HistoricTaskInstanceQuery taskBpmId(Long bpmId){
        taskBpmId = bpmId;
        return this;
    }

    @Override
    public HistoricTaskInstanceQuery finished() {
        finished = true;
        return this;
    }

    @Override
    public HistoricTaskInstanceQuery finishedBefore(LocalDateTime time) {
        this.finishedBefore = time;
        return this;
    }

    @Override
    public HistoricTaskInstanceQuery finishedAfter(LocalDateTime time) {
        this.finishedAfter = time;
        return this;
    }
}
