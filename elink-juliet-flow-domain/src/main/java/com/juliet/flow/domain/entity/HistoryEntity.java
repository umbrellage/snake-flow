package com.juliet.flow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * HistoryEntity
 *
 * @author Geweilang
 * @date 2023/8/4
 */
@Data
@TableName("jbpm_flow_history")
public class HistoryEntity extends BaseEntity {

    @TableId
    private Long id;

    private Long flowId;

    private Long mainFlowId;

    private Integer action;

    private Long sourceNodeId;

    private Long targetNodeId;

    private Long assignee;

    private String comment;


}
