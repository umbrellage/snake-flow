package com.juliet.flow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
@Data
@TableName("jbpm_flow_node_supplier")
public class SupplierEntity extends BaseEntity {

    @TableId
    private Long id;

    private Long nodeId;

    private String supplierType;

    private Long supplierId;
}
