package com.juliet.flow.client.dto;

import com.juliet.flow.client.common.NotifyTypeEnum;
import com.juliet.flow.client.common.TodoNotifyEnum;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.client.vo.ProcessedByVO;
import java.util.List;
import lombok.Data;

/**
 * NotifyDTO
 *
 * @author Geweilang
 * @date 2023/5/23
 */
@Data
public class NotifyDTO {
    private Long flowId;
    private String code;
    private Long mainFlowId;
    private Long nodeId;
    private List<String> filedList;
    /**
     * 主管ID列表
     */
    private List<Long> supervisorIds;
    private String nodeName;
    private NodeVO nodeVO;
    /**
     * 被通知人，被通知节点上的操作人，即对应原nodeId上的人
     */
    private Long userId;
    /**
     * 当前流程上的操作，当前这个flowId这个流程上操作了一下，影响到nodeId这个节点
     */
    private Long executorId;
    private List<String> postIdList;
    private NotifyTypeEnum type;
    private List<ProcessedByVO> preprocessedBy;
    private Long tenantId;
    private String remark;
    private TodoNotifyEnum todoNotify;
}
