package com.happy.chat.enums;

public enum ErrorEnum {
    SUCCESS(0, "成功"),
    SERVER_ERROR(10001, "服务异常"),
    UD_NOT_MATCHED(10002, "user验证失败"),
    OPERATION_NEED_LOGIN(10003, "该操作需要登录"),

    USER_NOT_EXIST(20001, "用户不存在"),
    PASSWORD_ERROR(20002, "密码错误"),
    PASSWORD_PATTERN_INVALID(20003, "密码格式有误"),

    USER_EMAIL_ALREADY_USED(30005, "该邮箱已被使用"),
    REG_USER_PWD_TOO_SHORT(30006, "注册用户密码过短"),
    REG_USER_PWD_TOO_LONG(30007, "注册用户密码过长"),
    REG_FAILED_BY_DUMMY_NOT_EXIST(30008, "注册失败dummy用户不存在"),

    EMAIL_PATTERN_INVALID(50001, "邮箱格式有误"),
    EMAIL_SEND_CODE_FAIL(50002, "发送邮箱验证码失败"),
    EMAIL_VERIFY_CODE_ERROR(50003, "邮箱验证码错误"),
    EMAIL_VERIFY_CODE_EXPIRE(50004, "邮箱验证码过期"),
    EMAIL_NOT_EXIST(50004, "邮箱不存在"),

    RESET_PASSWORD_FAIL(60001, "重置密码失败"),
    REBIND_EMAIL_FAIL(60002, "修改邮箱失败"),

    ROBOT_NOT_EXIST(70001, "AI不存在"),

    CHAT_NO_RESP(80001, "chat无响应"),
    STRIPE_PRICE_RETRIEVE_FAILED(90001, "stripe price获取失败"),
    STRIPE_PRICE_UN_CONFIG(90002, "未配置stripe priceId"),
    STRIPE_CREATE_SESSION_FAILED(90003, "stripe创建session失败"),


    ;

    private int errCode;

    private String errMsg;

    ErrorEnum(int errCode, String errMsg) {
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
