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
    public Long getAssignUserId(Map<String, Object> params, Flow flow, Long nodeId) {
        return null;
    }

    @Override
    public SupplierDTO getAssignSupplier(Map<String, Object> params) {
        String supplierId = String.valueOf(params.get("supplierId"));
        if (supplierId != null) {
            SupplierDTO dto = new SupplierDTO();
            dto.setSupplierId(String.valueOf(supplierId));
            dto.setSupplierType(null);
            return dto;
        }


        if (!params.containsKey("supplierSettled")) {
            return null;
        }
        Map<String, Object> map = (Map<String, Object>) params.get("supplierSettled");
        if (map.get("supplierId") == null || map.get("supplierType") == null) {
            return null;
        }
        SupplierDTO dto = new SupplierDTO();
        dto.setSupplierId(String.valueOf(map.get("supplierId")));
        dto.setSupplierType(String.valueOf(map.get("supplierType")));
        return dto;
    }
}
