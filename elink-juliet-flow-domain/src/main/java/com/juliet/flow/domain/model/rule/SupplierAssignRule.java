package com.juliet.flow.domain.model.rule;

import com.juliet.flow.client.dto.SupplierDTO;
import com.juliet.flow.domain.model.BaseAssignRule;
import com.juliet.flow.domain.model.Flow;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * SupplierAssignRule
 *
 * @author Geweilang
 * @date 2023/8/7
 */
@Component
public class SupplierAssignRule extends BaseAssignRule {

    @Override
    public String getRuleName() {
        return "assign_rule_supplier";
    }

    @Override
    public Long getAssignUserId(Map<String, Object> params, Flow flow) {
        return null;
    }

    @Override
    public SupplierDTO getAssignSupplier(Map<String, Object> params) {
        if (!params.containsKey("suppliersettled")) {
            return null;
        }
        Map<String, Object> map = (Map<String, Object>) params.get("suppliersettled");
        if (map.get("supplierId") == null || map.get("supplierType") == null) {
            return null;
        }
        SupplierDTO dto = new SupplierDTO();
        dto.setSupplierId((String) map.get("supplierId"));
        dto.setSupplierType(String.valueOf(map.get("supplierType")));
        return dto;
    }
}
