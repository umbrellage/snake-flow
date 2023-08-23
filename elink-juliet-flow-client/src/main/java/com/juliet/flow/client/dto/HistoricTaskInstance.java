package com.juliet.flow.client.dto;

import com.juliet.common.core.utils.DateUtils;
import com.juliet.common.core.utils.time.JulietTimeMemo;
import com.juliet.flow.client.common.OperateTypeEnum;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * HistoricTaskInstance
 *
 * @author Geweilang
 * @date 2023/8/22
 */
@Data
public class HistoricTaskInstance implements TaskInfo, HistoricData{

    private Long historyId;
    private Long flowId;
    private Long mainFlowId;
    private Long executeNodeId;
    private OperateTypeEnum operateType;
    private LocalDateTime createTime;


    @Override
    public Long historyId() {
        return historyId;
    }

    @Override
    public String createTime() {
        if (createTime == null) {
            return null;
        }
        return JulietTimeMemo.format(createTime, DateUtils.YYYY_MM_DD_HH_MM_SS);
    }

    @Override
    public Long flowId() {
        return flowId;
    }

    @Override
    public Long mainFlowId() {
        return mainFlowId;
    }

    @Override
    public Long executeNodeId() {
        return executeNodeId;
    }

    @Override
    public OperateTypeEnum operateType() {
        return operateType;
    }
}
