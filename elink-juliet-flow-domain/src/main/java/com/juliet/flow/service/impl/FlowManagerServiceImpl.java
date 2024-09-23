package com.juliet.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.juliet.common.core.exception.ServiceException;
import com.juliet.common.core.utils.DateUtils;
import com.juliet.common.core.utils.time.JulietTimeMemo;
import com.juliet.common.security.utils.SecurityUtils;
import com.juliet.flow.client.common.OperateTypeEnum;
import com.juliet.flow.client.common.TodoNotifyEnum;
import com.juliet.flow.client.vo.GraphEdgeVO;
import com.juliet.flow.client.vo.GraphEdgeVO.Property;
import com.juliet.flow.client.vo.PostVO;
import com.juliet.flow.client.common.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.FlowTemplate;
import com.juliet.flow.domain.model.History;
import com.juliet.flow.domain.model.Node;
import com.juliet.flow.client.vo.GraphNodeVO;
import com.juliet.flow.client.vo.GraphVO;
import com.juliet.flow.domain.model.Post;
import com.juliet.flow.repository.FlowRepository;
import com.juliet.flow.repository.HistoryRepository;
import com.juliet.flow.service.FlowManagerService;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author xujianjie
 * @date 2023-06-19
 */
@Service
@Slf4j
public class FlowManagerServiceImpl implements FlowManagerService {

    @Autowired
    private FlowRepository flowRepository;
    @Autowired
    private HistoryRepository historyRepository;

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

        if (flowTemplate.getDto() == null || isOldFabricFlow(flow, flowTemplate)) {
            String jsonFilePath = findJsonFile(flowTemplate);
            try {
                json = IOUtils.resourceToString(jsonFilePath, Charsets.toCharset("UTF-8"));
            } catch (IOException e) {
                log.error("read {} fail!", jsonFilePath, e);
            }
        } else {
            json = JSON.toJSONString(flowTemplate.getDto());
        }
        //
        List<History> historyList = historyRepository.queryByFlowId(id);
        vo = JSON.toJavaObject(JSON.parseObject(json), GraphVO.class);
//        fillDefaultRequire(vo);
        fillFlowInfo(flow, vo);
        fillEdgeInfo(flow, vo, historyList);
        return vo;
    }

    private boolean isOldFabricFlow(Flow flow, FlowTemplate flowTemplate) {
        if (!Arrays.asList("manage_sample", "match_sample", "mt_find_fabric").contains(flowTemplate.getCode())) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, 9, 23);
        calendar.set(Calendar.HOUR, 20);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        if (flow.getCreateTime().before(calendar.getTime())) {
            return true;
        }
        return false;
    }

    @Override
    public GraphVO getGraph(Long id, Long userId) {
        return getGraph(id, userId, Collections.emptyList());
    }

    @Override
    public GraphVO getGraph(Long flowId, Long userId, List<Long> postIdList) {
        Flow flow = flowRepository.queryById(flowId);
        Map<Long, Node> nodeMap = flow.getNodes().stream()
            .collect(Collectors.toMap(Node::getId, Function.identity()));
        GraphVO graphVO = getGraph(flowId);
        for (GraphNodeVO graphNodeVO : graphVO.getNodes()) {
            graphNodeVO.getProperties().setCanClick(canClick(graphNodeVO, flow, userId, graphNodeVO.getProperties()::setClickRemark, graphNodeVO.getProperties()::setCanClickError));
            graphNodeVO.getProperties().setCanAdjustment(canAdjustment(graphNodeVO, flow, userId));
            graphNodeVO.getProperties().setCanEdit(canEdit(graphNodeVO, flow, userId, postIdList));
            graphNodeVO.getProperties().setCurrentProcessUserId(getCurrentProcessBy(graphNodeVO, flow) != null ? String.valueOf(getCurrentProcessBy(graphNodeVO, flow)) : null);
            Long nodeId = getNodeIdByName(graphNodeVO, flow);
            if (nodeId != null) {
                graphNodeVO.getProperties().setNodeId(String.valueOf(nodeId));
                LocalDateTime time = nodeMap.get(nodeId).processedTime();
                Long processBy = nodeMap.get(nodeId).getProcessedBy();
                List<Post> postList = nodeMap.get(nodeId).getBindPosts();
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(postList)) {
                    List<PostVO> postVOList = postList.stream()
                        .filter(Objects::nonNull)
                        .map(Post::toPost)
                        .collect(Collectors.toList());
                    graphNodeVO.getProperties().setBindPost(postVOList);
                }
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
            flowTemplate = flowRepository.queryTemplateByCode(templateCode, 1L);
        }
        if (flowTemplate == null) {
            throw new ServiceException("没有找到流程模板，流程模板id:" + templateId);
        }
        GraphVO vo = null;
        String json = null;
        if (flowTemplate.getDto() == null) {
            String jsonFilePath = findJsonFile(flowTemplate);
            try {
                json = IOUtils.resourceToString(jsonFilePath, Charsets.toCharset("UTF-8"));
            } catch (IOException e) {
                log.error("read {} fail!", jsonFilePath, e);
            }
        } else {
            json = JSON.toJSONString(flowTemplate.getDto());
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

    private void fillEdgeInfo(Flow flow, GraphVO vo, List<History> historyList) {
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

            if (historyList.stream()
                    .filter(history -> history.getAction() == OperateTypeEnum.FORWARD)
                    .anyMatch(history -> Objects.equals(history.getSourceNodeId(), sourceNode.getId()) && Objects.equals(history.getTargetNodeId(), targetNode.getId()))) {
                property.setActivated(true);
                continue;
            }
            // 结束节点的线特殊处理
            if (targetNode.getType() == NodeTypeEnum.END && sourceNode.getStatus() == NodeStatusEnum.PROCESSED) {
                property.setActivated(true);
                continue;
            }
            // 这是旧的一种解决方案，通过节点的状态来处理，新的解决方案是通过流程流转的记录
//            // 表示这个节点被激活或者已经激活过操作完了，所以这条线不出意外是要被激活的
//            if (targetNode.getStatus() == NodeStatusEnum.ACTIVE ||
//                targetNode.getStatus() == NodeStatusEnum.TO_BE_CLAIMED ||
//                targetNode.getStatus() == NodeStatusEnum.PROCESSED) {
//                // 但是如果前置节点如果是ignore了那么这条线不应该被激活
//                if (sourceNode.getStatus() != NodeStatusEnum.IGNORE) {
//                    property.setActivated(true);
//                    continue;
//                }
//            }
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
            if (Objects.equals(graphNodeVO.getType(), "startNode")) {
                return true;
            }
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
                            consumer.accept("有流程将经过当前节点，无需操作，建议忽略");
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

    private boolean canEdit(GraphNodeVO graphNodeVO, Flow flow, Long userId, List<Long> postIdList) {
        List<Flow> subList = flowRepository.listFlowByParentId(flow.getId());
        Node currentNode = flow.getNodes()
            .stream()
            .filter(node -> isNodeMatched(node, graphNodeVO))
            .findAny()
            .orElse(null);

        if (currentNode == null) {
            return false;
        }
        List<Long> bindPostIdList = currentNode.getBindPosts().stream()
            .filter(Objects::nonNull)
            .map(Post::getPostId)
            .filter(Objects::nonNull)
            .map(Long::valueOf)
            .collect(Collectors.toList());
        List<Node> allSubNodeList = subList.stream()
            .map(Flow::getNodes)
            .flatMap(Collection::stream)
            .filter(subNode -> StringUtils.equals(subNode.getName(), currentNode.getName()))
            .collect(Collectors.toList());
        allSubNodeList.add(currentNode);


        return allSubNodeList.stream()
            .anyMatch(node -> {
                return (node.getStatus() == NodeStatusEnum.ACTIVE && Objects.equals(userId, node.getProcessedBy())) ||
                    (node.getStatus() == NodeStatusEnum.TO_BE_CLAIMED && !Collections.disjoint(postIdList, bindPostIdList));
            });
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
