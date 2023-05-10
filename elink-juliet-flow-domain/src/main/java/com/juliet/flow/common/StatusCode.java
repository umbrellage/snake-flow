package com.juliet.flow.common;

/**
 * @author xujianjie
 * @date 2023-05-09
 */
public enum StatusCode {

    SERVICE_SUCCESS(200, "操作成功"),
    SERVICE_ERROR(501, "错误:%s"),


    ;

    private int status;
    private String msg;

    StatusCode(int status, String message) {
        this.status = status;
        this.msg = message;
    }

    public boolean isSuccess() {
        return getStatus() == 200;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return name();
    }

    public String getMsg() {
        return msg;
    }

    public String getMsg(Object... objects) {
        if (objects == null) {
            return getMsg();
        }
        return String.format(msg, objects);
    }
}
