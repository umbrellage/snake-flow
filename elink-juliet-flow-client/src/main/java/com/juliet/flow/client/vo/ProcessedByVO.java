package com.juliet.flow.client.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.juliet.common.core.utils.DateUtils;
import com.juliet.flow.client.config.DateTime2String;
import com.juliet.flow.client.config.String2DateTimeDes;
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

    private Long nodeId;
    /**
     * 处理人
     */
    private Long processedBy;
    @JsonSerialize(using = DateTime2String.class)
    @JsonDeserialize(using = String2DateTimeDes.class)
    private LocalDateTime processedTime;

    public static ProcessedByVO of(Long nodeId, Long processedBy, LocalDateTime processedTime) {
        ProcessedByVO ret = new ProcessedByVO();
        ret.setNodeId(nodeId);
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
