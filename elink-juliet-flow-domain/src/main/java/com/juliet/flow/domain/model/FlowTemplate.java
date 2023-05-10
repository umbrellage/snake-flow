package com.juliet.flow.domain.model;

import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
import lombok.Data;
import org.springframework.util.CollectionUtils;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
public class FlowTemplate extends BaseModel {

    private Long id;

    private String name;

    private String code;

    private Node node;

    private FlowTemplateStatusEnum status;

    /**
     * 通过模板实例化一个流程
     */
    public Flow toFlowInstance() {
        Flow flow = new Flow();
        flow.setFlowTemplateId(this.id);
        flow.setNode(this.node);
        flow.setTenantId(getTenantId());
        cleanFlowId(flow);
        return flow;
    }

    public static void cleanFlowId(Flow flow) {
        flow.setId(null);
        cleanNodeId(flow.getNode());
    }

    public static void cleanNodeId(Node node) {
        node.setId(null);
        if (node.getForm() != null) {
            node.getForm().setId(null);
            if (!CollectionUtils.isEmpty(node.getForm().getFields())) {
                for (Field field : node.getForm().getFields()) {
                    field.setId(null);
                }
            }
        }
        if (CollectionUtils.isEmpty(node.getNext())) {
            return;
        }
        for (Node subNode : node.getNext()) {
            cleanNodeId(subNode);
        }
    }
}
