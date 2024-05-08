package com.juliet.flow.client.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Getter
@AllArgsConstructor
public enum FlowStatusEnum {

    /**
     *
     */
    IN_PROGRESS(1, "进行中"),
    ABNORMAL(2, "异常中"),
    END(3, "已结束"),
    INVALID(4, "已作废")
            ;

    private Integer code;

    private String msg;

    /**
     * 根据code查找
     * @param code
     * @return 枚举对象
     */
    public static String findMsgByCode(Integer code) {
        for (FlowStatusEnum statusEnum : FlowStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getMsg();
            }
        }
        throw new IllegalArgumentException("[NodeStatusEnum]code is invalid");
    }

    public static FlowStatusEnum findByCode(Integer code) {
        for (FlowStatusEnum statusEnum : FlowStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
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
        for (FlowStatusEnum statusEnum : values()) {
            if (statusEnum.getMsg().equals(msg)) {
                return statusEnum.getCode();
            }
        }
        throw new IllegalArgumentException("[NodeStatusEnum]msg is invalid");
    }
}
