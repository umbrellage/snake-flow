package com.juliet.flow.domain.model;

import com.juliet.flow.common.enums.FlowTemplateStatusEnum;
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
    public Flow toFlowInstance() {
        Flow flow = new Flow();
        flow.setFlowTemplateId(this.id);
        flow.setNodes(this.nodes);
        flow.setTenantId(getTenantId());
        cleanFlowId(flow);
        return flow;
    }

    public static void cleanFlowId(Flow flow) {
        flow.setId(null);
    }
}
