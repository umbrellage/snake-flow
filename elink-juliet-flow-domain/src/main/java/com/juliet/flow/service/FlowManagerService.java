package com.juliet.flow.service;

import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
public interface FlowManagerService {
    void add(FlowTemplate flowTemplate);

    void update(FlowTemplate flowTemplate);

    void publish(Long templateId);

    void disable(Long templateId);
}
