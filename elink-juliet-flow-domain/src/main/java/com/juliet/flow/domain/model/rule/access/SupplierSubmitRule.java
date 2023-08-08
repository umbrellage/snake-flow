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
    public boolean accessRule(Map<String, Object> params) {
        if (!params.containsKey("suppliersettled")) {
            return false;
        }
        Map<String, Object> map = (Map<String, Object>) params.get("suppliersettled");
        Boolean isLink = (Boolean) map.get("isLink");
        return !isLink;
    }
}
