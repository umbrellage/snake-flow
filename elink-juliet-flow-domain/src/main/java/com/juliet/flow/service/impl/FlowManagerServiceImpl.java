package com.juliet.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.client.vo.GraphNodeVO;
import com.juliet.flow.client.vo.GraphVO;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.service.FlowManagerService;
import java.util.function.Consumer;
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
        Flow flow = flowRepository.queryLatestByParentId(id);
        if (flow == null) {
            flow = flowRepository.queryById(id);
        }
        if (flow == null) {
            throw new ServiceException("没有找到流程，id:" + id);
        }
        FlowTemplate flowTemplate = flowRepository.queryTemplateById(flow.getFlowTemplateId());
        if (flowTemplate == null) {
            throw new ServiceException("没有找到流程模板，流程id:" + id + " 模板id:" + flow.getFlowTemplateId());
        }
        // todo 判断flow是否存在
        GraphVO vo = null;
        String json = null;
        String jsonFilePath = findJsonFile(flowTemplate);
        try {
            json = IOUtils.resourceToString(jsonFilePath, Charsets.toCharset("UTF-8"));
        } catch (IOException e) {
            log.error("read {} fail!", jsonFilePath, e);
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
            graphNodeVO.getProperties().setCanClick(canClick(graphNodeVO, flow, userId, graphNodeVO.getProperties()::setClickRemark, graphNodeVO.getProperties()::setCanClickError));
            graphNodeVO.getProperties().setCanAdjustment(canAdjustment(graphNodeVO, flow, userId));
            graphNodeVO.getProperties().setCurrentProcessUserId(String.valueOf(getCurrentProcessBy(graphNodeVO, flow)));
            graphNodeVO.getProperties().setNodeId(String.valueOf(getNodeIdByName(graphNodeVO, flow)));
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
            String text = getText(flow.getNodes(), graphNodeVO.getId());
            if (text != null) {
                graphNodeVO.getProperties().setText(text);
                if (graphNodeVO.getText() != null) {
                    graphNodeVO.getText().setValue(text);
                }
            }
        }
    }

    private String getText(List<Node> nodes, String graphNodeId) {
        for (Node node : nodes) {
            if (graphNodeId.equals(node.getName())) {
                return node.getTitle();
            }
        }
        return null;
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

    private boolean canClick(GraphNodeVO graphNodeVO, Flow flow, Long userId, Consumer<String> consumer, Consumer<Boolean> canClickError) {
        List<Flow> subList = flowRepository.listFlowByParentId(flow.getId());
        for (Node node : flow.getNodes()) {
            if (node.getName().equals(graphNodeVO.getId())) {
                if (node.getStatus() == NodeStatusEnum.PROCESSED && node.getProcessedBy().equals(userId)) {
                    if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(subList)) {
                        boolean flag = subList.stream().allMatch(e -> e.checkoutFlowNodeIsHandled(node.getName()));
                        if (!flag && consumer != null) {
                            consumer.accept("有流程将经过当前节点，不可变更");
                            canClickError.accept(Boolean.TRUE);
                        }
                        return flag;
                    }
                    return true;
                }
            }
        }
        consumer.accept("未找到可操作节点");
        return false;
    }

    private boolean canAdjustment(GraphNodeVO graphNodeVO, Flow flow, Long userId) {
        for (Node node : flow.getNodes()) {
            if (node.getName().equals(graphNodeVO.getId())) {
                if ((node.getSupervisorAssignment() != null && node.getSupervisorAssignment()) ||
                        (node.getSelfAndSupervisorAssignment() != null && node.getSelfAndSupervisorAssignment())) {
                    if (!CollectionUtils.isEmpty(node.getSupervisorIds())) {
                        return node.getSupervisorIds().stream().anyMatch(supervisorId -> supervisorId.equals(userId));
                    }
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

    private Long getNodeIdByName(GraphNodeVO graphNodeVO, Flow flow) {
        for (Node node : flow.getNodes()) {
            if (node.getName().equals(graphNodeVO.getId())) {
                return node.getId();
            }
        }
        return null;
    }

    private String findJsonFile(FlowTemplate flowTemplate) {
        return "/graph/" + flowTemplate.getCode() + ".json";
    }
}
