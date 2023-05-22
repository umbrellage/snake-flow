package com.juliet.flow.repository;

import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.domain.model.NodeQuery;

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

    List<Flow> queryByIdList(List<Long> idList);

    List<Flow> listFlowByParentId(Long id);

    Flow queryByCode(String code);

    void updateStatusById(FlowStatusEnum status, Long id);

    FlowTemplate queryTemplateById(Long id);

    FlowTemplate queryTemplateByCode(String code);

    void updateFlowTemplateStatusById(FlowTemplateStatusEnum status, Long id);

    List<Node> listNode(NodeQuery query);

    Node queryNodeById(Long nodeId);


}
