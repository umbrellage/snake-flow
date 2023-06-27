package com.juliet.flow.domain.model;

import com.juliet.common.core.exception.ServiceException;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.enums.NodeTypeEnum;
import java.time.LocalDateTime;
import java.util.Arrays;
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

    /**
     * 通过模板实例化一个流程
     */
    public Flow toFlowInstance(Long userId) {
        Flow flow = new Flow();
        flow.setFlowTemplateId(this.id);

        Node start = nodes.stream()
            .filter(node -> node.getType().equals(NodeTypeEnum.START))
            .findAny()
            .orElseThrow(() -> new ServiceException("找不到开始节点"));
        List<String> nextNameList = Arrays.stream(start.getNextName().split(",")).collect(Collectors.toList());
        nodes.forEach(node -> {
                if (nextNameList.contains(node.getName())) {
                    node.setStatus(NodeStatusEnum.TO_BE_CLAIMED);
                }
                if (node.getType().equals(NodeTypeEnum.START)) {
                    node.setStatus(NodeStatusEnum.PROCESSED);
                    node.setProcessedTime(LocalDateTime.now());
                    node.setProcessedBy(userId);
                }
            });
        flow.setNodes(nodes);
        flow.setTenantId(getTenantId());
        flow.setStatus(FlowStatusEnum.IN_PROGRESS);
        cleanFlowId(flow);
        return flow;
    }

    public static void cleanFlowId(Flow flow) {
        flow.setId(null);
    }
}
