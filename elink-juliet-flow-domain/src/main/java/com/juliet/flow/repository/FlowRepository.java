package com.juliet.flow.repository;

import com.juliet.flow.client.common.FlowStatusEnum;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.domain.model.NodeQuery;
import com.juliet.flow.domain.query.AssembleFlowCondition;

import java.util.Collection;
import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
public interface FlowRepository {

    Long add(Flow flow);

    Long addTemplate(FlowTemplate flowTemplate);

    void update(Flow flow);

    void updateTemplate(FlowTemplate flowTemplate);

    Flow queryById(Long id);

    void refreshCache(Long flowId);

    List<Flow> queryByIdList(List<Long> idList);

    List<Flow> queryByIdList(List<Long> idList,  AssembleFlowCondition condition);

    List<Flow> listFlowByParentId(Long id);

    List<Flow> listFlowByParentId(Collection<Long> idList);

    List<Flow> listFlowByParentId(Collection<Long> idList,  AssembleFlowCondition condition);

    List<Flow> queryMainFlowById(Collection<Long> idList);

    FlowTemplate queryTemplateById(Long id);

    FlowTemplate queryTemplateByCode(String code);

    FlowTemplate queryTemplateByCode(String code, Long tenantId);

    void updateFlowTemplateStatusById(FlowTemplateStatusEnum status, Long id);

    List<Node> listNode(NodeQuery query);

    List<Node> listNode(Long supplierId, String supplierType);

    Node queryNodeById(Long nodeId);

    void deleteFlow(Long id);

    /**
     * 查出由我参与的流程
     * @param flowCode 流程code
     * @param userId 用户id
     * @return
     */
    List<Flow> listFlow(String flowCode, Long userId, List<Long> postIdList);

}