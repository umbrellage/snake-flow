package com.juliet.flow.domain.model;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.juliet.flow.client.dto.BpmDTO;
import com.juliet.flow.client.vo.GraphEdgeVO;
import com.juliet.flow.client.vo.GraphNodeVO;
import com.juliet.flow.client.vo.GraphVO;
import org.apache.commons.lang3.StringUtils;

/**
 * TempGraphContext
 *
 * @author Geweilang
 * @date 2024/9/24
 */
public final class TempGraphContext {

    private static final TransmittableThreadLocal<GraphVO> GRAPH_LOCAL = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<Flow> FLOW_LOCAL = new TransmittableThreadLocal<>();

    public static void putFlow(Flow flow) {
        if (flow == null) {
            return;
        }
        FLOW_LOCAL.set(flow);
    }

    public static Flow getFlow() {
        return FLOW_LOCAL.get();
    }


    public static void putGraphVO(GraphVO graph) {
        if (graph == null) {
            return;
        }
        GRAPH_LOCAL.set(graph);
    }

    public static GraphVO getGraphVO() {
        return GRAPH_LOCAL.get();
    }


    public static void clean() {
        GRAPH_LOCAL.remove();
        FLOW_LOCAL.remove();
    }


    /**
     * 临时兼容线上匹样单bug用，返回一个
     * @param flow
     * @return
     */
    public static Node preNode(Flow flow, Node currentNode) {
        if (flow == null || currentNode == null) {
            return null;
        }

        return flow.getNodes().stream()
            .filter(node -> StringUtils.equals(node.getNextName(), currentNode.getName()))
            .findFirst()
            .orElse(null);
    }
    /**
     * 临时兼容线上匹样单bug用，返回一个,为了方便后面删除，一起写在这里
     * @return
     */
    public static GraphNodeVO preGraphNode(GraphVO graph, GraphNodeVO graphNode) {
        if (graph == null || graphNode == null) {
            return null;
        }

        GraphEdgeVO preEdge = graph.getEdges().stream()
            .filter(edge -> edge.getTargetNodeId().equals(graphNode.getId()))
            .findFirst()
            .orElse(null);
        if (preEdge == null) {
            return null;
        }
        return graph.getNodes().stream()
            .filter(node -> node.getId().equals(preEdge.getSourceNodeId()))
            .findFirst()
            .orElse(null);
    }


    public static boolean preNodeBothEq(GraphNodeVO graphNode, Node node) {
        if (graphNode == null || node == null) {
            return false;
        }
        GraphVO graphVO = TempGraphContext.getGraphVO();
        Flow flow = TempGraphContext.getFlow();
        Node preNode = preNode(flow, node);
        GraphNodeVO preGraphNodeVO = preGraphNode(graphVO, graphNode);
        if (preGraphNodeVO == null || preNode == null) {
            return false;
        }
        return preGraphNodeVO.getProperties().getText().equals(preNode.getTitle());
    }



}
