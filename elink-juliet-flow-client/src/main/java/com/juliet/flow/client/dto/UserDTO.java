package com.juliet.flow.client.dto;

import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * UserDTO
 * 用户信息
 * @author Geweilang
 * @date 2023/5/10
 */
@Getter
@Setter
public class UserDTO {

    private Long userId;
    private Long tenantId;
    private List<Long> postId;
    private SupplierDTO supplier;

    public static SupplierDTO of(String supplierType, Long supplierId) {
        SupplierDTO dto = new SupplierDTO();
        dto.setSupplierId(supplierId);
        dto.setSupplierType(supplierType);
        return dto;
    }

    public String supplierType() {
        return supplier != null ? supplier.getSupplierType() : null;
    }

    public Long supplierId() {
        return supplier != null ? supplier.getSupplierId() : null;
    }
    @Data
    static class SupplierDTO {
        private Long supplierId;
        private String supplierType;
    }
}
