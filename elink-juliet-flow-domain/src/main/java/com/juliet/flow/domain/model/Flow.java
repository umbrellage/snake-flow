package com.juliet.flow.domain.model;

import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.enums.NodeStatusEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;

import java.util.Collection;
import java.util.List;

/**
 *
 * 流程
 *
 * @author xujianjie
 * @date 2023-05-06
 */
@Data
public class Flow {

    private Long id;

    private String name;
    /**
     * 当流程为子流程时，存在父流程
     */
    private Long parentId;

    private Long flowTemplateId;

    private Node node;

    private FlowStatusEnum status;

    private Long tenantId;

    public boolean forward() {
        return true;
    }

    public Todo getCurrentTodo() {
        return new Todo();
    }

    /**
     * 当前流程是否已经结束
     */
    public boolean isEnd() {
        return true;
    }

    /**
     * 返回当前流程中处理的节点
     */
    private Node getCurrentActiveNode() {
        return new Node();
    }

    public void validate() {
        BusinessAssert.assertNotNull(this.node, StatusCode.SERVICE_ERROR, "不能没有节点信息!");
    }

    /**
     * 图遍历，获取节点
     * todo 待测试
     * @param nodeStatusList 节点状态
     * @return 节点列表
     */
    public List<Node> getNodeByNodeStatus(List<NodeStatusEnum> nodeStatusList) {
        if (CollectionUtils.isEmpty(nodeStatusList)) {
            return Collections.emptyList();
        }
        List<Node> output = new ArrayList<>();
        LinkedList<Node> stack = new LinkedList<>();
        stack.add(this.node);
        while (!stack.isEmpty()) {
            Node node = stack.pollLast();
            if (nodeStatusList.contains(node.getStatus())) {
                output.add(node);
            }
            if (CollectionUtils.isNotEmpty(node.getNext())) {
                Collections.reverse(node.getNext());
                stack.addAll(node.getNext());
            }
        }
        return output.stream()
            .distinct()
            .collect(Collectors.toList());
    }
}
