package com.juliet.flow.domain.model;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.client.dto.ProcessConfigRPCDTO;
import com.juliet.flow.client.common.FlowStatusEnum;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.client.common.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.Data;

import java.util.List;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class FlowTemplate extends BaseModel {

    private Long id;

    private String name;

    private String code;

    private List<Node> nodes;

    private FlowTemplateStatusEnum status;

    private ProcessConfigRPCDTO dto;

    /**
     * 通过模板实例化一个流程
     */
    public Flow toFlowInstance(Long userId) {
        Flow flow = new Flow();
        flow.setFlowTemplateId(this.id);
        flow.setTenantId(getTenantId());
        List<Node> nodeList = nodes.stream()
            .map(Node::copyNode)
            .collect(Collectors.toList());
        Node start = nodeList.stream()
            .filter(node -> node.getType().equals(NodeTypeEnum.START))
            .findAny()
            .orElseThrow(() -> new ServiceException("找不到开始节点"));
        start.setStatus(NodeStatusEnum.ACTIVE);
        start.setProcessedTime(LocalDateTime.now());
        start.setProcessedBy(userId);
        flow.setNodes(nodeList);
        flow.setTenantId(getTenantId());
        start.setClaimTime(LocalDateTime.now());
        start.setActiveTime(LocalDateTime.now());
        flow.setStatus(FlowStatusEnum.IN_PROGRESS);
        cleanFlowId(flow);
        return flow;
    }

    public static void cleanFlowId(Flow flow) {
        flow.setId(null);
    }
}
