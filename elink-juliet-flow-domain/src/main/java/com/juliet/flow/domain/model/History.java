package com.juliet.flow.domain.model;

import com.juliet.common.core.utils.time.JulietTimeMemo;
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

    private OperateTypeEnum action;

    private Long sourceNodeId;

    private Long targetNodeId;

    private Long triggerNode;

    private Long assignee;

    private String comment;

    private Long tenantId;

    private LocalDateTime createTime;


    public static History of(HistoryEntity entity) {
        History data = new History();
        data.setId(entity.getId());
        data.setFlowId(entity.getFlowId());
        data.setMainFlowId(entity.getMainFlowId());
        data.setAction(OperateTypeEnum.of(entity.getAction()));
        data.setSourceNodeId(entity.getSourceNodeId());
        data.setTargetNodeId(entity.getTargetNodeId());
        data.setTriggerNode(entity.getTriggerNode());
        data.setAssignee(entity.getAssignee());
        data.setComment(entity.getComment());
        data.setTenantId(entity.getTenantId());
        data.setCreateTime(JulietTimeMemo.toDateTime(entity.getCreateTime()));
        return data;
    }

    public HistoryEntity to() {
        HistoryEntity entity = new HistoryEntity();
        BeanUtils.copyProperties(this, entity);
        entity.setCreateTime(new Date());
        return entity;
    }



    public static History of(RollbackDTO dto, Long targetNodeId, Flow flow) {
        History data = new History();
        data.setFlowId(Long.valueOf(dto.getFlowId()));
        data.setAction(OperateTypeEnum.ROLLBACK);
        data.setSourceNodeId(Long.valueOf(dto.getNodeId()));
        data.setTargetNodeId(targetNodeId);
        data.setAssignee(dto.getUserId());
        data.setTriggerNode(Long.valueOf(dto.getNodeId()));
        data.setComment(dto.getReason());
        data.setTenantId(flow.getTenantId());
        data.setMainFlowId(flow.getParentId());
        return data;
    }

    public static History of(RejectDTO dto, Long targetNodeId, Flow flow) {
        History data = new History();
        data.setFlowId(Long.valueOf(dto.getFlowId()));
        data.setAction(OperateTypeEnum.REJECT);
        data.setSourceNodeId(Long.valueOf(dto.getNodeId()));
        data.setTriggerNode(Long.valueOf(dto.getNodeId()));
        data.setTargetNodeId(targetNodeId);
        data.setAssignee(dto.getUserId());
        data.setComment(dto.getReason());
        data.setTenantId(flow.getTenantId());
        data.setMainFlowId(flow.getParentId());
        return data;
    }

    public static History of(Flow flow, Long userId, Long sourceNodeId, Long targetNodeId) {
        History data = new History();
        data.setFlowId(flow.getId());
        data.setMainFlowId(flow.getParentId());
        data.setAction(OperateTypeEnum.FORWARD);
        data.setSourceNodeId(sourceNodeId);
        data.setTargetNodeId(targetNodeId);
        data.setTriggerNode(sourceNodeId);
        data.setAssignee(userId);
        data.setTenantId(flow.getTenantId());
        return data;
    }

    public static History errorClose(Flow flow, Long targetNodeId, Long triggerNodeId) {
        History data = new History();
        data.setFlowId(flow.getId());
        data.setMainFlowId(flow.getParentId());
        data.setAction(OperateTypeEnum.ERROR_CLOSE);
        data.setTargetNodeId(targetNodeId);
        data.setTriggerNode(triggerNodeId);
        data.setTenantId(flow.getTenantId());
        return data;
    }

}
