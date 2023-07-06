package com.happy.chat.uitls;

import static com.happy.chat.constants.Constant.ERROR_CODE;
import static com.happy.chat.constants.Constant.ERROR_MESSAGE;

import java.util.HashMap;
import java.util.Map;

import com.happy.chat.enums.ApiErrorEnum;

public class ApiResult {
    public static Map<String, Object> ofSuccess() {
        Map<String, Object> result = new HashMap<>();
        ;
        result.put(ERROR_CODE, ApiErrorEnum.SUCCESS.getErrCode());
        result.put(ERROR_MESSAGE, ApiErrorEnum.SUCCESS.getErrMsg());
        return result;
    }

    public static Map<String, Object> ofFail(ApiErrorEnum apiErrorEnum) {
        Map<String, Object> result = new HashMap<>();
        result.put(ERROR_CODE, apiErrorEnum.getErrCode());
        result.put(ERROR_MESSAGE, apiErrorEnum.getErrMsg());
        return result;
    }
}
