package com.juliet.flow.client.dto;

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

    private String title;

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
     * 绑定的供应商
     */
    private List<SupplierDTO> bindSuppliers;

    @NotNull
    private Integer status;

    private String customStatus;

    /**
     * TodoNotifyEnum
     */
    private Integer todoNotify;

    private String modifyOtherTodoName;

    private String tenantId;

}
