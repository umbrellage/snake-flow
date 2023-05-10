package com.juliet.flow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xujianjie
 * @date 2023-05-06
 */
@Getter
@AllArgsConstructor
public enum FlowTemplateStatusEnum {

    IN_PROGRESS(1, "编辑中"),
    ABNORMAL(2, "已启用"),
    END(3, "已禁用"),
            ;

    private Integer code;

    private String msg;

    /**
     * 根据code查找
     * @param code
     * @return 枚举对象
     */
    public static String findMsgByCode(Integer code) {
        for (FlowTemplateStatusEnum statusEnum : FlowTemplateStatusEnum.values()) {
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
        for (FlowTemplateStatusEnum statusEnum : values()) {
            if (statusEnum.getMsg().equals(msg)) {
                return statusEnum.getCode();
            }
        }
        throw new IllegalArgumentException("[NodeStatusEnum]msg is invalid");
    }

    public static FlowTemplateStatusEnum byCode(Integer code) {
        for (FlowTemplateStatusEnum statusEnum : FlowTemplateStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("[NodeStatusEnum]code is invalid");
    }
}
