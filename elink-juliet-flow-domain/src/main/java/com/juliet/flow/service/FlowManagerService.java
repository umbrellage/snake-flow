package com.juliet.flow.service;

import com.juliet.flow.client.vo.GraphVO;

/**
 * @author xujianjie
 * @date 2023-06-19
 */
public interface FlowManagerService {

    GraphVO getGraph(Long id);

    GraphVO getGraph(Long id, Long userId);

    GraphVO getTemplateGraph(Long templateId);
}
