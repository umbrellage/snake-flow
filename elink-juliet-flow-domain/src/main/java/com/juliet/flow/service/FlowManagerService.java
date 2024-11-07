package com.juliet.flow.service;

import com.juliet.flow.client.vo.GraphVO;
import com.juliet.flow.domain.dto.FlowFormFieldsUpdateDTO;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-06-19
 */
public interface FlowManagerService {

    GraphVO getGraph(Long id);

    GraphVO getGraph(Long id, Long userId);

    GraphVO getGraph(Long flowId, Long userId, List<Long> postIdList);

    GraphVO getTemplateGraph(Long templateId, String templateCode);

    void updateFlowFormFields(FlowFormFieldsUpdateDTO dto);
}
