package com.juliet.flow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.juliet.flow.domain.model.Node;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.Data;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
@TableName("jbpm_flow_node")
public class NodeEntity extends BaseEntity {

    @TableId
    private Long id;

    private String title;

    @TableField(value = "node_name")
    private String name;

    private String preName;

    private String nextName;

    private Long flowId;

    private Long flowTemplateId;

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
    private Integer supervisorAssignment;

    /**
     * 认领+调整
     */
    private Integer selfAndSupervisorAssignment;

    /**
     * 规则分配
     */
    private Integer ruleAssignment;

    /**
     * 分配规则
     */
    private String assignRuleName;

    /**
     * 主管ID列表
     */
    @TableField(value = "supervisor_ids")
    private String supervisorIds;

    @TableField(value = "node_status")
    private Integer status;

    private String customStatus;

    @TableField(value = "node_type")
    private Integer type;

    private Long processedBy;

    private Integer todoNotify;

    private String modifyOtherTodoName;

    /**
     * supervisorId 格式修改，如需修改前后缀字符，请一起修改以下方法, 并考虑历史数据
     * @see Node#formatOf
     *
     * @return
     */
    public List<Long> supervisorIds() {
        if (StringUtils.isNotBlank(supervisorIds)) {
            return Arrays.stream(supervisorIds.split(","))
                .map(e -> StringUtils.remove(e, "^"))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
