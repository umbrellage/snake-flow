package com.juliet.flow.client.dto;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * HistoryTaskInstance
 *
 * @author Geweilang
 * @date 2024/7/12
 */
public interface HistoryTaskInstance {

    /**
     * 任务id,这里用节点id表示吧
     * @return taskId
     */
    Long id();
    /**
     * 操作人
     * @return taskId
     */
    Long taskAssignee();

    /**
     * Time when the task was created.
     * node activeTime
     * @return LocalDateTime
     */
    LocalDateTime taskCreateTime();

    /**
     * Time when the task was completed
     * @return LocalDateTime
     */
    LocalDateTime taskEndTime();

    /**
     * Difference between {@link #taskEndTime()} and {@link #taskClaimTime()}
     * @return Duration
     */
    Duration getWorkTimeInMillis();

    /**
     * Difference between {@link #taskEndTime()} and {@link #taskCreateTime()} in milliseconds.
     * @return Duration
     */
    Duration getDurationInMillis();


    /**
     * Time when the task was claimed.
     * @return LocalDateTime
     */
    LocalDateTime taskClaimTime();
}
