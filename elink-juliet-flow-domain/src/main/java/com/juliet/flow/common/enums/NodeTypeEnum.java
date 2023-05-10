package com.juliet.flow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Getter
@AllArgsConstructor
public enum NodeTypeEnum {

    START(1, "开始"),
    HANDLE(2, "处理"),
    END(3, "结束")
            ;

    private Integer code;

    private String msg;

    /**
     * 根据code查找
     * @param code
     * @return 枚举对象
     */
    public static String findMsgByCode(Integer code) {
        for (NodeTypeEnum statusEnum : NodeTypeEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getMsg();
            }
        }
        throw new IllegalArgumentException("[NodeTypeEnum]code is invalid");
    }

    /**
     * 根据msg查找
     * @param msg
     * @return 枚举对象
     */
    public static Integer findCodeByMsg(String msg) {
        for (NodeTypeEnum statusEnum : values()) {
            if (statusEnum.getMsg().equals(msg)) {
                return statusEnum.getCode();
            }
        }
        throw new IllegalArgumentException("[NodeTypeEnum]msg is invalid");
    }

    public static NodeTypeEnum byCode(Integer code) {
        for (NodeTypeEnum statusEnum : NodeTypeEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("[NodeTypeEnum]code is invalid");
    }
}
