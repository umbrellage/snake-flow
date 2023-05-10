package com.juliet.flow.service;

import com.juliet.flow.domain.dto.FlowTemplateAddDTO;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
public interface FlowTemplateService {
    void add(FlowTemplateAddDTO flowTemplateAddDTO);

    void update(FlowTemplateAddDTO flowTemplateAddDTO);

    void publish(Long flowTemplateId);

    void disable(Long flowTemplateId);
}
