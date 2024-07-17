package com.juliet.flow.client.dto;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * HistoryTaskInstance
 *
 * @author Geweilang
 * @date 2024/7/12
 */
@Data
public class HistoryTaskInstance implements Serializable {

    /**
     * 任务id,这里用节点id表示吧
     */
    private Long id;
    /**
     * 操作人
     */
    private Long taskAssignee;

    /**
     * Time when the task was created.
     * node activeTime
     */
    private LocalDateTime taskCreateTime;

    /**
     * Time when the task was completed
     */
    private LocalDateTime taskEndTime;

    /**
     * Difference between {@link #taskEndTime} and {@link #taskClaimTime}
     */
    private Duration workTimeInMillis;

    /**
     * Difference between {@link #taskEndTime} and {@link #taskCreateTime} in milliseconds.
     */
    private Duration durationInMillis;


    /**
     * Time when the task was claimed.
     */
    private LocalDateTime taskClaimTime;


    public Duration getWorkTimeInMillis() {
        if (taskClaimTime == null || taskEndTime == null) {
            return null;
        }
        return Duration.between(taskClaimTime, taskEndTime);
    }

    public Duration getDurationInMillis() {
        if (taskCreateTime == null || taskEndTime == null) {
            return null;
        }
        return Duration.between(taskCreateTime, taskEndTime);
    }
}
