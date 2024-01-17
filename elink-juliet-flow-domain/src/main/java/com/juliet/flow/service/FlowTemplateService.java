package com.juliet.flow.service;

import com.juliet.flow.client.dto.FlowTemplateAddDTO;
import com.juliet.flow.domain.model.FlowTemplate;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
public interface FlowTemplateService {
    void add(FlowTemplateAddDTO flowTemplateAddDTO);

    void update(FlowTemplateAddDTO flowTemplateAddDTO);

    FlowTemplate queryById(Long id);

    void publish(Long flowTemplateId);

    void disable(Long flowTemplateId);
}
