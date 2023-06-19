package com.juliet.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.vo.GraphNodeVO;
import com.juliet.flow.domain.vo.GraphVO;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.service.FlowManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
            json = IOUtils.resourceToString("/graph/flow_touyang.json", Charsets.toCharset("UTF-8"));
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
        if (vo == null || CollectionUtils.isEmpty(vo.getNodes())) {
            return;
        }
        for (GraphNodeVO nodeVO : vo.getNodes()) {
            if (Arrays.asList("caa3d868-4216-4c46-b10b-64f5c9822654",
                    "999b28d5-ce23-44be-8c94-0acdde6c180e",
                    "6d6d5eb0-72ec-4a97-bc73-4c95984f0fd9").contains(nodeVO.getId())) {
                nodeVO.getProperties().setActive(true);
            }
        }
    }
}
