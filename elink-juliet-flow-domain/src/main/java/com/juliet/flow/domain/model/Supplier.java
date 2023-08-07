package com.juliet.flow.domain.model;

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
}
