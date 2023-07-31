package com.happy.chat.uitls;

import static com.happy.chat.constants.Constant.ERROR_CODE;
import static com.happy.chat.constants.Constant.ERROR_MESSAGE;
import static com.happy.chat.constants.Constant.ERROR_URL;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.exception.ServiceException;

public class ApiResult {
    public static Map<String, Object> ofSuccess() {
        Map<String, Object> result = new HashMap<>();
        result.put(ERROR_CODE, ErrorEnum.SUCCESS.getErrCode());
        result.put(ERROR_MESSAGE, ErrorEnum.SUCCESS.getErrMsg());
        return result;
    }

    public static Map<String, Object> ofFail(ErrorEnum apiErrorEnum) {
        Map<String, Object> result = new HashMap<>();
        result.put(ERROR_CODE, apiErrorEnum.getErrCode());
        result.put(ERROR_MESSAGE, apiErrorEnum.getErrMsg());
        return result;
    }

    public static Map<String, Object> ofError(ServiceException exception) {
        Map<String, Object> result = new HashMap<>();
        result.put(ERROR_CODE, exception.getCode());
        result.put(ERROR_MESSAGE, exception.getMessage());
        if (StringUtils.isNotEmpty(exception.getErrorUrl())) {
            result.put(ERROR_URL, exception.getErrorUrl());
        }
        return result;
    }
}
