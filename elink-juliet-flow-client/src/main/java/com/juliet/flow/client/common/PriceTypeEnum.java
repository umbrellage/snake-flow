package com.juliet.flow.client.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 价格类型
 *
 * @author xujianjie
 * @date 2023-04-21
 */
@Getter
@AllArgsConstructor
public enum PriceTypeEnum {

    ITEM_PRICE_MATERIAL_DEV_MAX_INCLUDE_TAX(1, "面料最高开发价(含税)"),
    ITEM_PRICE_MATERIAL_DEV_MAX_EXCLUDE_TAX(2, "面料最高开发价(不含税)"),
    ITEM_PRICE_MATERIAL_ANALYSIS_MAX_INCLUDE_TAX(3, "面料最高分析价(含税)"),
    ITEM_PRICE_MATERIAL_ANALYSIS_MAX_EXCLUDE_TAX(4, "面料最高分析价(不含税)"),
    ITEM_PRICE_MATERIAL_CHECK_MAX_INCLUDE_TAX(5, "面料最高审批价(含税)"),
    ITEM_PRICE_MATERIAL_CHECK_MAX_EXCLUDE_TAX(6, "面料最高开发价(不含税)"),
    ;

    private Integer code;

    private String msg;

    /**
     * 根据code查找
     * @param code
     * @return 枚举对象
     */
    public static String findMsgByCode(Integer code) {
        for (PriceTypeEnum statusEnum : PriceTypeEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getMsg();
            }
        }
        throw new IllegalArgumentException("[PriceTypeEnum]code is invalid");
    }

    /**
     * 根据msg查找
     * @param msg
     * @return 枚举对象
     */
    public static Integer findCodeByMsg(String msg) {
        for (PriceTypeEnum statusEnum : PriceTypeEnum.values()) {
            if (statusEnum.getMsg().equals(msg)) {
                return statusEnum.getCode();
            }
        }
        throw new IllegalArgumentException("[BizCodeEnum]msg is invalid");
    }
}
