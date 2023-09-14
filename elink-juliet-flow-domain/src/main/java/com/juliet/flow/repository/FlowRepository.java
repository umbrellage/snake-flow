package com.juliet.flow.repository;

import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.domain.model.NodeQuery;

import java.util.Collection;
import java.util.List;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
public interface FlowRepository {

    Long add(Flow flow);

    void addTemplate(FlowTemplate flowTemplate);

    void update(Flow flow);

    void updateTemplate(FlowTemplate flowTemplate);

    Flow queryById(Long id);

    /**
     * 获取最新的异常流程
     * @param id 主流程id
     * @return
     */
    Flow queryLatestByParentId(Long id);

    List<Flow> queryByIdList(List<Long> idList);

    List<Flow> queryOnlyFlowByIdList(List<Long> idList);

    List<Flow> listFlowByIdOrParentId(List<Long> idList);

    List<Flow> listFlowByParentId(Long id);

    List<Flow> listFlowByParentId(Collection<Long> idList);

    List<Flow> queryMainFlowById(Collection<Long> idList);

    void updateStatusById(FlowStatusEnum status, Long id);

    FlowTemplate queryTemplateById(Long id);

    FlowTemplate queryTemplateByCode(String code, Long tenantId);

    void updateFlowTemplateStatusById(FlowTemplateStatusEnum status, Long id);

    List<Node> listNode(NodeQuery query);

    List<Node> listNode(Long supplierId, String supplierType);

    Node queryNodeById(Long nodeId);

    void deleteFlow(Long id);

}