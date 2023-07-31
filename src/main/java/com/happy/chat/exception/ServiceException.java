package com.happy.chat.exception;

import java.util.HashMap;
import java.util.Map;

import com.happy.chat.enums.ErrorEnum;

/**
 * 所有的业务都抛这一个异常
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;
    private final String message;
    private final Map<String, Object> payload = new HashMap<>();
    private final String errorUrl;

    public static ServiceException ofEnum(ErrorEnum apiErrorEnum) {
        return new ServiceException(apiErrorEnum.getErrCode(), apiErrorEnum.getErrMsg());
    }
    public static ServiceException ofMessage(int code, String message) {
        return new ServiceException(code, message);
    }

    public static ServiceException ofMessage(int code, String message, String errorUrl) {
        return new ServiceException(code, message, errorUrl);
    }

    private ServiceException(int code, String message) {
        this.code = code;
        this.message = message;
        this.errorUrl = null;
    }

    private ServiceException(int code, String message, String errorUrl) {
        this.code = code;
        this.message = message;
        this.errorUrl = errorUrl;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getErrorUrl() {
        return errorUrl;
    }
}
