package com.juliet.flow.service;

import com.juliet.flow.domain.vo.GraphVO;

/**
 * @author xujianjie
 * @date 2023-06-19
 */
public interface FlowManagerService {

    GraphVO getGraph(Long id);

    GraphVO getTemplateGraph(Long templateId);
}