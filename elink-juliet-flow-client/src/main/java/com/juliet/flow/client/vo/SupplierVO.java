package com.juliet.flow.client.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xujianjie
 * @date 2023-07-28
 */
@Data
public class SupplierVO implements Serializable {

    private Long id;

    private String supplierType;

    private Long supplierId;

}
