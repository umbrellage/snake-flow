package com.juliet.flow.domain.model;

import com.juliet.flow.client.dto.HistoryTaskInstance;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * HistoryTaskInstanceImpl
 *
 * @author Geweilang
 * @date 2024/7/13
 */
@Data
public class HistoryTaskInstanceImpl implements HistoryTaskInstance {

    private Long id;
    private LocalDateTime taskCreateTime;
    private LocalDateTime taskEndTime;
    private LocalDateTime taskClaimTime;


    @Override
    public Long id() {
        return id;
    }

    @Override
    public LocalDateTime taskCreateTime() {
        return taskCreateTime;
    }

    @Override
    public LocalDateTime taskEndTime() {
        return taskEndTime;
    }

    @Override
    public Duration getWorkTimeInMillis() {
        if (taskClaimTime == null || taskEndTime == null) {
            return null;
        }
        return Duration.between(taskClaimTime, taskEndTime);
    }

    @Override
    public Duration getDurationInMillis() {
        if (taskCreateTime == null || taskEndTime == null) {
            return null;
        }
        return Duration.between(taskCreateTime, taskEndTime);
    }

    @Override
    public LocalDateTime taskClaimTime() {
        return taskClaimTime;
    }
}
