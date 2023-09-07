package com.juliet.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.juliet.common.core.exception.ServiceException;
import com.juliet.common.core.utils.DateUtils;
import com.juliet.common.core.utils.time.JulietTimeMemo;
import com.juliet.common.security.utils.SecurityUtils;
import com.juliet.flow.client.common.TodoNotifyEnum;
import com.juliet.flow.client.vo.FlowVO;
import com.juliet.flow.client.vo.GraphEdgeVO;
import com.juliet.flow.client.vo.GraphEdgeVO.Property;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.client.vo.GraphNodeVO;
import com.juliet.flow.client.vo.GraphVO;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.service.FlowManagerService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
//        fillDefaultRequire(vo);
        fillFlowInfo(flow, vo);
        fillEdgeInfo(flow, vo);
        return vo;
    }

    @Override
    public GraphVO getGraph(Long id, Long userId) {
        Flow flow = flowRepository.queryById(id);
        Map<Long, Node> nodeMap = flow.getNodes().stream()
            .collect(Collectors.toMap(Node::getId, Function.identity()));
        GraphVO graphVO = getGraph(id);
        for (GraphNodeVO graphNodeVO : graphVO.getNodes()) {
            graphNodeVO.getProperties().setCanClick(canClick(graphNodeVO, flow, userId, graphNodeVO.getProperties()::setClickRemark, graphNodeVO.getProperties()::setCanClickError));
            graphNodeVO.getProperties().setCanAdjustment(canAdjustment(graphNodeVO, flow, userId));
            graphNodeVO.getProperties().setCurrentProcessUserId(String.valueOf(getCurrentProcessBy(graphNodeVO, flow)));
            Long nodeId = getNodeIdByName(graphNodeVO, flow);
            if (nodeId != null) {
                graphNodeVO.getProperties().setNodeId(String.valueOf(nodeId));
                LocalDateTime time = nodeMap.get(nodeId).getProcessedTime();
                Long processBy = nodeMap.get(nodeId).getProcessedBy();
                graphNodeVO.getProperties().setProcessBy(processBy);
                if (time != null) {
                    graphNodeVO.getProperties().setOperateTime(JulietTimeMemo.format(time, DateUtils.YYYY_MM_DD_HH_MM_SS));
                }
            }
        }
        return graphVO;
    }

    @Override
    public GraphVO getTemplateGraph(Long templateId, String templateCode) {
        FlowTemplate flowTemplate = null;
        if (templateId != null) {
            flowTemplate = flowRepository.queryTemplateById(templateId);
        } else if (templateCode != null) {
            flowTemplate = flowRepository.queryTemplateByCode(templateCode, SecurityUtils.getLoginUser().getSysUser().getTenantId());
        }
        if (flowTemplate == null) {
            throw new ServiceException("没有找到流程模板，流程模板id:" + templateId);
        }
        GraphVO vo = null;
        String json = null;
        String jsonFilePath = findJsonFile(flowTemplate);
        try {
            json = IOUtils.resourceToString(jsonFilePath, Charsets.toCharset("UTF-8"));
        } catch (IOException e) {
            log.error("read {} fail!", jsonFilePath, e);
        }
        vo = JSON.toJavaObject(JSON.parseObject(json), GraphVO.class);
        return vo;
    }

    private void fillFlowInfo(Flow flow, GraphVO vo) {
        if (vo == null || CollectionUtils.isEmpty(vo.getNodes())) {
            return;
        }
        for (GraphNodeVO graphNodeVO : vo.getNodes()) {
            graphNodeVO.getProperties().setActivated(isActive(flow.getNodes(), graphNodeVO));
            graphNodeVO.getProperties().setFinished(isFinished(flow.getNodes(), graphNodeVO));
            graphNodeVO.getProperties().setDisabled(isDisabled(flow.getNodes(), graphNodeVO));
            graphNodeVO.getProperties().setRequired(isRequire(flow.getNodes(), graphNodeVO));
            String text = getText(flow.getNodes(), graphNodeVO);
            if (text != null) {
                graphNodeVO.getProperties().setText(text);
                if (graphNodeVO.getText() != null) {
                    graphNodeVO.getText().setValue(text);
                }
            }
        }
    }

    private void fillEdgeInfo(Flow flow, GraphVO vo) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(vo.getEdges()) ||
            org.apache.commons.collections4.CollectionUtils.isEmpty(vo.getNodes())) {
            return;
        }
        for (GraphEdgeVO edge: vo.getEdges()) {
            String sourceNodeId = edge.getSourceNodeId();
            String targetNodeId = edge.getTargetNodeId();
            GraphNodeVO sourceNodeGraph = vo.getNodes().stream()
                .filter(node -> StringUtils.equals(sourceNodeId, node.getId()))
                .findAny()
                .orElseThrow(() -> new ServiceException("流程图配置有问题，你再去检查下"));
            GraphNodeVO targetNodeGraph = vo.getNodes().stream()
                .filter(node -> StringUtils.equals(targetNodeId, node.getId()))
                .findAny()
                .orElseThrow(() -> new ServiceException("流程图配置有问题，你再去检查下"));
            Node sourceNode = flow.getNodes().stream()
                .filter(node -> isNodeMatched(node, sourceNodeGraph))
                .findAny()
                .orElse(null);
            Node targetNode = flow.getNodes().stream()
                .filter(node -> isNodeMatched(node, targetNodeGraph))
                .findAny()
                .orElse(null);
            Property property = edge.getProperties();
            if (property == null) {
                property = new Property();
            }
            if (sourceNode == null || targetNode == null) {
                property.setActivated(false);
                continue;
            }
            // 表示这个节点被激活或者已经激活过操作完了，所以这条线不出意外是要被激活的
            if (targetNode.getStatus() == NodeStatusEnum.ACTIVE ||
                targetNode.getStatus() == NodeStatusEnum.TO_BE_CLAIMED ||
                targetNode.getStatus() == NodeStatusEnum.PROCESSED) {
                // 但是如果前置节点如果是ignore了那么这条线不应该被激活
                if (sourceNode.getStatus() != NodeStatusEnum.IGNORE) {
                    property.setActivated(true);
                    continue;
                }
            }
            property.setActivated(false);
        }
    }

    private String getText(List<Node> nodes, GraphNodeVO graphNodeVO) {
        for (Node node : nodes) {
            if (isNodeMatched(node, graphNodeVO)) {
                return node.getTitle();
            }
        }
        return null;
    }

    private boolean isActive(List<Node> nodes, GraphNodeVO graphNodeVO) {
        for (Node node : nodes) {
            if (isNodeMatched(node, graphNodeVO)) {
                if (node.getStatus() == NodeStatusEnum.ACTIVE || node.getStatus() == NodeStatusEnum.TO_BE_CLAIMED) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRequire(List<Node> nodes, GraphNodeVO graphNodeVO) {
        for (Node node : nodes) {
            if (isNodeMatched(node, graphNodeVO)) {
                return node.getTodoNotify() == TodoNotifyEnum.NOTIFY;
            }
        }
        return false;
    }

    private boolean isDisabled(List<Node> nodes, GraphNodeVO graphNodeVO) {
        for (Node node : nodes) {
            if (isNodeMatched(node, graphNodeVO)) {
                return node.getStatus() == NodeStatusEnum.IGNORE;
            }
        }
        return false;
    }

    private boolean isFinished(List<Node> nodes, GraphNodeVO graphNodeVO) {
        for (Node node : nodes) {
            if (isNodeMatched(node, graphNodeVO)) {
                return node.getStatus() == NodeStatusEnum.PROCESSED;
            }
        }
        return false;
    }

    private boolean canClick(GraphNodeVO graphNodeVO, Flow flow, Long userId, Consumer<String> consumer, Consumer<Boolean> canClickError) {
        List<Flow> subList = flowRepository.listFlowByParentId(flow.getId());
        for (Node node : flow.getNodes()) {
            if (isNodeMatched(node, graphNodeVO)) {
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
            if (isNodeMatched(node, graphNodeVO)) {
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
            if (isNodeMatched(node, graphNodeVO)) {
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
            if (isNodeMatched(node, graphNodeVO)) {
                return node.getId();
            }
        }
        return null;
    }

    private String findJsonFile(FlowTemplate flowTemplate) {
        if (flowTemplate.getCode().startsWith("supplier_settled")) {
            return "/graph/supplier_settled.json";
        }
        return "/graph/" + flowTemplate.getCode() + ".json";
    }

    private boolean isNodeMatched(Node node, GraphNodeVO graphNodeVO) {
        return node.getName().equals(graphNodeVO.getId()) || node.getName().equals(graphNodeVO.getProperties().getName());
    }

    /**
     * 默认不设计required时，为true，即：有通知的待办节点
     */
    private void fillDefaultRequire(GraphVO graphVO) {
        graphVO.getNodes().forEach(graphNodeVO -> {
            if (graphNodeVO.getProperties().getRequired() == null) {
                graphNodeVO.getProperties().setRequired(true);
            }
        });
    }
}
