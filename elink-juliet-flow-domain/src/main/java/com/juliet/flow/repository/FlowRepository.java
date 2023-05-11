package com.juliet.flow.repository;

import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;
import java.util.List;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
public interface FlowRepository {

    void add(Flow flow);

    void addTemplate(FlowTemplate flowTemplate);

    void update(Flow flow);

    void updateTemplate(FlowTemplate flowTemplate);

    Flow queryById(Long id);

    List<Flow> listFlowByParentId(Long id);

    Flow queryByCode(String code);

    void updateStatusById(FlowStatusEnum status, Long id);

    FlowTemplate queryTemplateById(Long id);

    FlowTemplate queryTemplateByCode(String code);

    void updateFlowTemplateStatusById(FlowTemplateStatusEnum status, Long id);

    /**
     * todo
     * 查询子流程
     * @param id
     * @return
     */
    List<Flow> querySubFlowById(Long id);

    /**
     * todo 创建一个子流程，将nodeId 设置为已处理，nodeId 后的所有已处理的node 装态设置为已认领
     * 创建一个子流程
     * @param flowId 流程id
     * @param nodeId 节点id
     */
    void addSubFlow(Long flowId, Long nodeId);
}
