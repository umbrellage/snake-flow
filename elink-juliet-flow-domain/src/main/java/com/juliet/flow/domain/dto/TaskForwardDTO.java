package com.juliet.flow.domain.dto;

import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.Node;
import java.util.Map;
import lombok.Data;

/**
 * TaskForwardDTO
 *
 * @author Geweilang
 * @date 2023/8/22
 */
@Data
public class TaskForwardDTO {

    /**
     * 主流程id
     */
    private Flow mainFlow;
    /**
     * 操作节点
     */
    private Node executeNode;
    /**
     * 操作人
     */
    private Long executeId;
    private Map<String, Object> data;

    public static TaskForwardDTO valueOf(Flow flow, Node node, Long userId, Map<String, Object> data) {
        TaskForwardDTO res = new TaskForwardDTO();
        res.setData(data);
        res.setExecuteId(userId);
        res.setExecuteNode(node);
        res.setMainFlow(flow);
        return res;
    }

}
