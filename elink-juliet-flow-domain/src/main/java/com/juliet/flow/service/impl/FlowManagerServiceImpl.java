package com.juliet.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.client.vo.GraphNodeVO;
import com.juliet.flow.client.vo.GraphVO;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.service.FlowManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

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
        // todo 判断flow是否存在
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
    public GraphVO getGraph(Long id, Long userId) {
        Flow flow = flowRepository.queryById(id);
        GraphVO graphVO = getGraph(id);
        for (GraphNodeVO graphNodeVO : graphVO.getNodes()) {
            graphNodeVO.getProperties().setCanClick(canClick(graphNodeVO, flow, userId));
            graphNodeVO.getProperties().setCurrentProcessUserId(String.valueOf(getCurrentProcessBy(graphNodeVO, flow)));
        }
        return graphVO;
    }

    @Override
    public GraphVO getTemplateGraph(Long templateId) {
        return null;
    }

    private void fillFlowInfo(Flow flow, GraphVO vo) {
        if (vo == null || CollectionUtils.isEmpty(vo.getNodes())) {
            return;
        }
        for (GraphNodeVO graphNodeVO : vo.getNodes()) {
            graphNodeVO.getProperties().setActive(isActive(flow.getNodes(), graphNodeVO.getId()));
        }
    }

    private boolean isActive(List<Node> nodes, String graphNodeId) {
        for (Node node : nodes) {
            if (graphNodeId.equals(node.getName())) {
                if (node.getStatus() == NodeStatusEnum.ACTIVE || node.getStatus() == NodeStatusEnum.TO_BE_CLAIMED) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canClick(GraphNodeVO graphNodeVO, Flow flow, Long userId) {
        for (Node node : flow.getNodes()) {
            if (node.getName().equals(graphNodeVO.getId())) {
                if (node.getStatus() == NodeStatusEnum.PROCESSED && node.getProcessedBy().equals(userId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Long getCurrentProcessBy(GraphNodeVO graphNodeVO, Flow flow) {
        for (Node node : flow.getNodes()) {
            if (node.getName().equals(graphNodeVO.getId())) {
                if (node.getProcessedBy() != null && node.getProcessedBy() == 0L) {
                    return null;
                }
                return node.getProcessedBy();
            }
        }
        return null;
    }
}
