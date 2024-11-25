package com.juliet.flow.client.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class NodeDTO {

    private String id;

    private String externalNodeId;

    private String title;

    /**
     * 工序的Id，从档案字段来
     */
    private Long titleId;

    @NotNull
    private String name;

    @NotNull
    private FormDTO form;

    @NotNull
    private String preName;

    @NotNull
    private String nextName;

    /**
     * 准入规则
     */
    private String accessRuleName;

    private String flowAutomateRuleName;

    /**
     * 提交规则
     */
    private String submitRuleName;

    /**
     * 主管分配
     */
    private Boolean supervisorAssignment;

    /**
     * 认领+调整
     */
    private Boolean selfAndSupervisorAssignment;

    /**
     * 规则分配
     */
    private Boolean ruleAssignment;

    @ApiModelProperty("是否分配给流程内的节点操作人")
    private Boolean flowInnerAssignment;

    @ApiModelProperty("从分配的节点里获取操作人")
    private String distributeNode;

    /**
     * 分配规则
     */
    private String assignRuleName;

    /**
     * 主管ID列表
     */
    private List<Long> supervisorIds;

    /**
     * 值参考NodeTypeEnum
     */
    @NotNull
    private Integer type;

    /**
     * 绑定的岗位ID
     */
    private List<PostDTO> bindPosts;

    /**
     * 岗位下可以认领的人
     */
    private List<Long> claimableUserIds;

    /**
     * 绑定的供应商
     */
    private List<SupplierDTO> bindSuppliers;

    @NotNull
    private Integer status;

    private String customStatus;

    private Long customStatusId;

    /**
     * TodoNotifyEnum
     */
    private Integer todoNotify;

    private String modifyOtherTodoName;

    private String tenantId;

    private List<AssignmentRuleDTO> assignmentRuleList;

    private List<AccessRuleDTO> accessRuleList;

    private List<AccessRuleDTO> forwardRuleList;

    private List<AccessRuleDTO> rollbackRuleList;

}
