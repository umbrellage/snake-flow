package com.juliet.flow.domain.model;

import com.juliet.flow.client.common.OperateTypeEnum;
import com.juliet.flow.client.dto.RejectDTO;
import com.juliet.flow.client.dto.RollbackDTO;
import com.juliet.flow.domain.entity.HistoryEntity;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * History
 *
 * @author Geweilang
 * @date 2023/8/4
 */
@Data
public class History {

    private Long id;

    private Long flowId;

    private Long mainFlowId;

    private Integer action;

    private Long sourceNodeId;

    private Long targetNodeId;

    private Long assignee;

    private String comment;

    private Long tenantId;

    private LocalDateTime createTime;


    public HistoryEntity to() {
        HistoryEntity entity = new HistoryEntity();
        BeanUtils.copyProperties(this, entity);
        entity.setCreateTime(new Date());
        return entity;
    }


    public static History of(RollbackDTO dto, Long targetNodeId, Long tenantId) {
        History data = new History();
        data.setFlowId(Long.valueOf(dto.getFlowId()));
        data.setAction(OperateTypeEnum.ROLLBACK.getCode());
        data.setSourceNodeId(Long.valueOf(dto.getNodeId()));
        data.setTargetNodeId(targetNodeId);
        data.setAssignee(dto.getUserId());
        data.setComment(dto.getReason());
        data.setTenantId(tenantId);
        return data;
    }

    public static History of(RejectDTO dto, Long targetNodeId, Long tenantId) {
        History data = new History();
        data.setFlowId(Long.valueOf(dto.getFlowId()));
        data.setAction(OperateTypeEnum.REJECT.getCode());
        data.setSourceNodeId(Long.valueOf(dto.getNodeId()));
        data.setTargetNodeId(targetNodeId);
        data.setAssignee(dto.getUserId());
        data.setComment(dto.getReason());
        data.setTenantId(tenantId);
        return data;
    }
}
