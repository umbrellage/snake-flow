package com.juliet.flow.client.dto;

import com.juliet.flow.client.common.NotifyTypeEnum;
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
     * 主管分配
     */
    private Boolean supervisorAssignment;

    /**
     * 认领+调整
     */
    private Boolean selfAndSupervisorAssignment;
    private String nodeName;
    private Long userId;
    private List<String> postIdList;
    private NotifyTypeEnum type;
    private List<ProcessedByVO> preprocessedBy;
    private Long tenantId;

}
