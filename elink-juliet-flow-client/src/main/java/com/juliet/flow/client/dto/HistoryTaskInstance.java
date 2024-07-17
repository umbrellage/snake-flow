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
    private Duration getWorkTimeInMillis;

    /**
     * Difference between {@link #taskEndTime} and {@link #taskCreateTime} in milliseconds.
     */
    private Duration getDurationInMillis;


    /**
     * Time when the task was claimed.
     */
    private LocalDateTime taskClaimTime;
}
