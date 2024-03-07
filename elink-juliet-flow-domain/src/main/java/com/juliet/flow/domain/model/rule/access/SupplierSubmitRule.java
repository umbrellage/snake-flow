package com.juliet.flow.domain.model.rule.access;

import com.juliet.flow.domain.model.BaseRule;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * SupplierSubmitRule
 *
 * @author Geweilang
 * @date 2023/8/8
 */
@Component
public class SupplierSubmitRule extends BaseRule {

    @Override
    public String getRuleName() {
        return "supplier_settle_submit";
    }

    @Override
    public boolean accessRule(Map<String, Object> params, Long nodeId) {
        if (!params.containsKey("supplierSettled")) {
            return false;
        }
        Map<String, Object> map = (Map<String, Object>) params.get("supplierSettled");
        Boolean isLink = (Boolean) map.get("isLink");
        return !isLink;
    }
}
