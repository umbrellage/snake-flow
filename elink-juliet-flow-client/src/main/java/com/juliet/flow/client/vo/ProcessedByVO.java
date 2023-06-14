package com.juliet.flow.client.vo;

import com.juliet.common.core.utils.DateUtils;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * TodoPersonVO
 *
 * @author Geweilang
 * @date 2023/5/26
 */
@Getter
@Setter
public class ProcessedByVO {

    /**
     * 处理人
     */
    private Long processedBy;
    private LocalDateTime processedTime;

    public static ProcessedByVO of(Long processedBy, LocalDateTime processedTime) {
        ProcessedByVO ret = new ProcessedByVO();
        ret.setProcessedBy(processedBy);
        ret.setProcessedTime(processedTime);
        return ret;
    }

    public Date processedTime() {
        if (processedTime == null) {
            return null;
        }
        return DateUtils.toDate(processedTime);
    }
}
