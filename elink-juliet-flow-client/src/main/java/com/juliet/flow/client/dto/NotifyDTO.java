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
     * 主管ID列表
     */
    private List<Long> supervisorIds;
    private String nodeName;
    private Long userId;
    private List<String> postIdList;
    private NotifyTypeEnum type;
    private List<ProcessedByVO> preprocessedBy;
    private Long tenantId;
    private String remark;

}
