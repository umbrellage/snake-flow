package com.juliet.flow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Getter
@AllArgsConstructor
public enum NodeStatusEnum {


    /**
     * 未到流程
     */
    NOT_ACTIVE(1, "未激活"),
    TO_BE_CLAIMED(2, "待认领"),
    ACTIVE(3, "已认领"),
    PROCESSED(4, "已处理"),
    IGNORE(5, "忽略")



            ;

    private Integer code;

    private String msg;

    /**
     * 根据code查找
     * @param code
     * @return 枚举对象
     */
    public static String findMsgByCode(Integer code) {
        for (NodeStatusEnum statusEnum : NodeStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getMsg();
            }
        }
        throw new IllegalArgumentException("[NodeStatusEnum]code is invalid");
    }

    /**
     * 根据msg查找
     * @param msg
     * @return 枚举对象
     */
    public static Integer findCodeByMsg(String msg) {
        for (NodeStatusEnum statusEnum : values()) {
            if (statusEnum.getMsg().equals(msg)) {
                return statusEnum.getCode();
            }
        }
        throw new IllegalArgumentException("[NodeStatusEnum]msg is invalid");
    }

    /**
     * 根据code查找
     * @param code
     * @return 枚举对象
     */
    public static NodeStatusEnum byCode(Integer code) {
        for (NodeStatusEnum statusEnum : NodeStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("[NodeStatusEnum]code is invalid");
    }
}
