package com.juliet.flow.domain.model;

import com.juliet.flow.common.StatusCode;
import com.juliet.flow.common.enums.FlowStatusEnum;
import com.juliet.flow.common.utils.BusinessAssert;
import lombok.Data;
import org.apache.commons.compress.utils.Lists;
import org.springframework.util.CollectionUtils;

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

    private String templateId;

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
}
