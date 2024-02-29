package com.juliet.flow.service;

import com.juliet.flow.client.dto.FlowTemplateAddDTO;
import com.juliet.flow.client.vo.NodeVO;
import com.juliet.flow.domain.model.FlowTemplate;
import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
public interface FlowTemplateService {
    Long add(FlowTemplateAddDTO flowTemplateAddDTO);

    void update(FlowTemplateAddDTO flowTemplateAddDTO);

    FlowTemplate queryById(Long id);

    FlowTemplate queryByCode(String code);

    void publish(Long flowTemplateId);

    void disable(Long flowTemplateId);

    List<NodeVO> nodeList(Long id);
}
