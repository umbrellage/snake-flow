package com.juliet.flow.client.vo;

import com.juliet.flow.client.common.OperateTypeEnum;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.juliet.flow.client.config.DateTime2String;
import com.juliet.flow.client.config.String2DateTimeDes;
import lombok.Getter;
import lombok.Setter;

/**
 * NodeVO
 *
 * @author Geweilang
 * @date 2023/5/10
 */
@Getter
@Setter
public class NodeVO {

    private Long id;

    private String externalNodeId;

    private Long tenantId;

    /**
     * 模版code
     */
    private String code;

    private String title;

    private String name;

    private String preName;

    private String nextName;

    /**
     * NOT_ACTIVE(1, "未激活"), TO_BE_CLAIMED(2, "待认领"), ACTIVE(3, "已认领"), PROCESSED(4, "已处理")
     */
    private Integer status;

    private String customStatus;

    private Long flowId;

    private Long mainFlowId;

    /**
     * 主管分配
     */
    private Boolean supervisorAssignment;

    /**
     * 认领+调整
     */
    private Boolean selfAndSupervisorAssignment;

    /**
     * 表单
     */
    private FormVO form;

    private List<PostVO> bindPosts;

    private List<SupplierVO> bindSuppliers;

    /**
     * 主管ID列表
     */
    private List<Long> supervisorIds;

    /**
     * 上一个处理人
     */
    private List<ProcessedByVO> preprocessedBy;

    /**
     * 处理人
     */
    private Long processedBy;

    private String remark;
    /**
     * 节点最新的操作
     */
    private OperateTypeEnum operateType;

    @JsonSerialize(using = DateTime2String.class)
    @JsonDeserialize(using = String2DateTimeDes.class)
    private LocalDateTime processedTime;

    public NodeSimpleVO toSimple() {
        NodeSimpleVO ret = new NodeSimpleVO();
        ret.setId(id);
        ret.setName(name);
        ret.setFlowId(flowId);
        ret.setProcessedBy(processedBy);

        return ret;
    }

    public String distinct() {
        Long id;
        if (mainFlowId != null && mainFlowId != 0) {
            id = mainFlowId;
        } else {
            id = flowId;
        }
        return id + name;
    }
}
