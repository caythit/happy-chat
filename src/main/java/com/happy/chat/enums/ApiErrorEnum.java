package com.happy.chat.enums;

public enum ApiErrorEnum {
    SUCCESS(0, "成功"),
    LOGIN_USER_NOT_EXIST(201, "登录用户不存在"),
    LOGIN_PWD_ERROR(202, "登录密码错误"),
    ;

    private int errCode;

    private String errMsg;

    ApiErrorEnum(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
