package com.juliet.flow.client.dto;

import com.juliet.flow.client.common.OperateTypeEnum;

/**
 * TaskInfo
 *
 * @author Geweilang
 * @date 2023/8/23
 */
public interface TaskInfo {

    Long flowId();

    Long mainFlowId();

    Long executeNodeId();

    OperateTypeEnum operateType();

}
