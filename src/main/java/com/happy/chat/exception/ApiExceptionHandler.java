package com.happy.chat.exception;

import static com.happy.chat.enums.ErrorEnum.SERVER_ERROR;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.happy.chat.uitls.ApiResult;


@ControllerAdvice
public class ApiExceptionHandler {

    @ResponseBody
    @ExceptionHandler(ServiceException.class)
    public Map<String, Object> handleException(ServiceException exception) {
        return ApiResult.ofError(exception);
    }

    @ResponseBody
    @ExceptionHandler(Throwable.class)
    public Map<String, Object> handleAllException(HttpServletRequest request,
                                                  Throwable exception) {
        ServiceException serviceException = ServiceException.ofMessage(SERVER_ERROR.getErrCode(), SERVER_ERROR.getErrMsg());
        return ApiResult.ofError(serviceException);
    }
}
