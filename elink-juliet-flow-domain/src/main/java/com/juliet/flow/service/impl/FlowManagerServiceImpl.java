package com.juliet.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.vo.GraphVO;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.service.FlowManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author xujianjie
 * @date 2023-06-19
 */
@Service
@Slf4j
public class FlowManagerServiceImpl implements FlowManagerService {

    @Autowired
    private FlowRepository flowRepository;

    @Override
    public GraphVO getGraph(Long id) {
        Flow flow = flowRepository.queryById(id);
        GraphVO vo = null;
        String json = null;
        try {
            ClassPathResource classPathResource = new ClassPathResource("graph/flow_touyang.json");
            byte[] binaryData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            json = new String(binaryData, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("read flow_touyang.json fail!", e);
        }
        vo = JSON.toJavaObject(JSON.parseObject(json), GraphVO.class);
        fillFlowInfo(flow, vo);
        return vo;
    }

    @Override
    public GraphVO getTemplateGraph(Long templateId) {
        return null;
    }

    private void fillFlowInfo(Flow flow, GraphVO vo) {

    }
}
