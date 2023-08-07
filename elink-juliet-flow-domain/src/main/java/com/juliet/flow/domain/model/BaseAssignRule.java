package com.juliet.flow.domain.model;

import lombok.Data;

import java.util.Map;

/**
 * @author xujianjie
 * @date 2023-06-14
 */
public abstract class BaseAssignRule {

    public abstract String getRuleName();

    public abstract Long getAssignUserId(Map<String, Object> params, Flow flow);

    public abstract AssignSupplier getAssignSupplier(Map<String, Object> params, Flow flow);

    @Data
    public static class AssignSupplier {

        private String supplierType;

        private Long supplierId;
    }
}
