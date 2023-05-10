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
public enum ItemEntityFieldEnum {

    /**
     * 商品主表预定义字段
     */
    ID("id", "id"),
    TENANT_ID("tenantId", "租户ID"),
    ITEM_NO("itemNo", "商品编号"),
    CATEGORY_ID("categoryId", "类目ID"),
    CATEGORY_NAME("categoryName", "类目名称"),
    BRAND_ID("brandId", "品牌ID"),
    BRAND_NAME("brandName", "品牌名称"),
    ITEM_NAME("itemName", "商品名称"),
    ITEM_TYPE("itemType", "商品类型，面料、辅助、..."),
    STATUS("status", "状态"),
    MAIN_PIC_URL("mainPicUrl", "主图"),
    SHOW_PIC_URL("showPicUrl", "其他图"),
    DEL_FLAG("delFlag", "删除标"),
    CREATE_TIME("createTime", "创建时间"),
    UPDATE_TIME("updateTime", "更新时间"),
    CREATE_BY("createBy", "创建人"),
    UPDATE_BY("updateBy", "更新人"),
    REP_TIME("repTime", "系统时间"),
    ;

    private String name;

    private String msg;


    /**
     * 通过name查找对应的枚举值
     */
    public static ItemEntityFieldEnum byName(String name) {
        if (name == null) {
            return null;
        }
        for (ItemEntityFieldEnum itemEntityFieldEnum : values()) {
            if (name.equals(itemEntityFieldEnum.getName())) {
                return itemEntityFieldEnum;
            }
        }
        return null;
    }
}
