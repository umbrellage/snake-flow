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
public enum ItemTypeEnum {

    ITEM_TYPE_MATERIAL(1, "面料"),
    ITEM_TYPE_ACCESSORIES(2, "辅料"),
    ;

    private Integer code;

    private String msg;

    /**
     * 根据code查找
     * @param code
     * @return 枚举对象
     */
    public static String findMsgByCode(Integer code) {
        for (ItemTypeEnum statusEnum : ItemTypeEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getMsg();
            }
        }
        throw new IllegalArgumentException("[ItemType]code is invalid");
    }

    /**
     * 根据msg查找
     * @param msg
     * @return 枚举对象
     */
    public static Integer findCodeByMsg(String msg) {
        for (ItemTypeEnum statusEnum : ItemTypeEnum.values()) {
            if (statusEnum.getMsg().equals(msg)) {
                return statusEnum.getCode();
            }
        }
        throw new IllegalArgumentException("[ItemType]msg is invalid");
    }
}
