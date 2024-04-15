package com.juliet.flow.domain.model;

import com.juliet.flow.common.utils.IdGenerator;
import java.util.Date;
import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-07-28
 */
@Data
public class Supplier extends BaseModel {

    private Long id;

    private String supplierType;

    private Long supplierId;

    private String supplierName;

    public Supplier deepCopy() {
        Supplier ret = new Supplier();
        ret.setId(IdGenerator.getId());
        ret.setSupplierId(supplierId);
        ret.setSupplierName(supplierName);
        ret.setSupplierType(supplierType);
        ret.setTenantId(getTenantId());
        ret.setCreateBy(getCreateBy());
        ret.setUpdateBy(getUpdateBy());
        ret.setCreateTime(new Date());
        ret.setUpdateTime(new Date());
        return ret;
    }
}
