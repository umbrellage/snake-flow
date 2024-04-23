package com.juliet.flow.domain.model.rule.access;

import com.juliet.flow.domain.model.BaseRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author xujianjie
 * @date 2024-04-18
 */
@Component
public class AccessoryStockUseOutAmountAuditAccessRule extends BaseRule {

    @Override
    public String getRuleName() {
        return "accessory_stock_use_out_amount_audit";
    }

    @Override
    public boolean accessRule(Map<String, Object> params, Long nodeId) {
        Object objQuantity = params.get("amount");
        if (objQuantity == null) {
            return true;
        }
        BigDecimal quantity = new BigDecimal(objQuantity.toString());
        return quantity.compareTo(new BigDecimal("1000")) >= 0;
    }
}
