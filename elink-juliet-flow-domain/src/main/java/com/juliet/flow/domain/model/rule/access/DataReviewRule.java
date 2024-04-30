package com.juliet.flow.domain.model.rule.access;

import com.juliet.flow.client.common.NodeStatusEnum;
import com.juliet.flow.constant.FlowConstant;
import com.juliet.flow.domain.model.BaseRule;
import com.juliet.flow.domain.model.Flow;
import com.juliet.flow.domain.model.Node;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * DataReviewRule
 *
 * @author Geweilang
 * @date 2023/8/8
 */
@Component
public class DataReviewRule extends BaseRule {

    @Override
    public String getRuleName() {
        return "supplier_settle_data_review";
    }

    /**
     * isLink：true， 资料初审
     * @param params
     * @return
     */
    @Override
    public boolean accessRule(Map<String, Object> params, Long nodeId) {
        if (!params.containsKey("supplierSettled")) {
            return false;
        }
        Flow flow = (Flow) params.get(FlowConstant.INNER_FLOW);
        Node node = (Node) params.get(FlowConstant.CURRENT_NODE);
        Map<String, Object> map = (Map<String, Object>) params.get("supplierSettled");
        Boolean isLink = (Boolean) map.get("isLink");



        return isLink || node.preNameList().stream()
            .allMatch(name -> flow.findNode(name).getStatus() == NodeStatusEnum.PROCESSED);
    }
}
