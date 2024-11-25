package com.juliet.flow.client.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.Getter;

/**
 * HistoricTaskQueryObject
 *
 * @author Geweilang
 * @date 2024/7/12
 */
@Getter
public class HistoricTaskQueryObject implements Serializable {

    /**
     * 操作人
     */
    private Long taskAssignee;

    /**
     * 操作人
     */
    private Collection<Long> taskAssignees;
    /**
     * 流程模版id
     */
    @Deprecated
    private Long taskBpmId;

    private Collection<Long> taskBpmIdList;


    private Collection<String> taskBpmCodeList;
    private Boolean finished;
    /**
     * 流程实例id
     */
    private Long processInstanceId;
    /**
     * 流程实例id列表
     */
    private Collection<Long> processInstanceIds;
    private LocalDateTime finishedBefore;
    private LocalDateTime finishedAfter;


    public HistoricTaskQueryObject processInstanceIds(Collection<Long> processInstanceIds) {
        this.processInstanceIds = processInstanceIds;
        return this;
    }

    public HistoricTaskQueryObject taskBpmCodeList(Collection<String> taskBpmCodeList) {
        this.taskBpmCodeList = taskBpmCodeList;
        return this;
    }

    public HistoricTaskQueryObject processInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public HistoricTaskQueryObject taskAssignee(Long assignee) {
        taskAssignee = assignee;
        return this;
    }

    public HistoricTaskQueryObject taskAssignee(Set<Long> assignees) {
        taskAssignees = assignees;
        return this;
    }
    @Deprecated
    public HistoricTaskQueryObject taskBpmId(Long bpmId){
        taskBpmId = bpmId;
        return this;
    }

    public HistoricTaskQueryObject taskBpmId(Collection<Long> bpmIdList){
        this.taskBpmIdList = bpmIdList;
        return this;
    }

    public HistoricTaskQueryObject finished() {
        finished = true;
        return this;
    }

    public HistoricTaskQueryObject finishedBefore(LocalDateTime time) {
        finished = true;
        this.finishedBefore = time;
        return this;
    }

    public HistoricTaskQueryObject finishedAfter(LocalDateTime time) {
        finished = true;
        this.finishedAfter = time;
        return this;
    }

    private HistoricTaskQueryObject() {

    }


    public static HistoricTaskQueryObject createHistoricTaskQuery() {
        return new HistoricTaskQueryObject();
    }

}