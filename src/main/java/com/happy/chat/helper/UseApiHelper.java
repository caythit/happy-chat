package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.DATA;
import static com.happy.chat.uitls.CommonUtils.decryptPwd;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.happy.chat.enums.ApiErrorEnum;
import com.happy.chat.model.User;
import com.happy.chat.model.UserGetRequest;
import com.happy.chat.service.PaymentService;
import com.happy.chat.service.UserService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.view.UserView;

import lombok.extern.slf4j.Slf4j;


@Lazy
@Component
@Slf4j
public class UseApiHelper {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;


    public Map<String, Object> doLogin(String userName, String password) {
        Map<String, Object> result = ApiResult.ofSuccess();
        // 根据用户名直接去db拿数据
        User user = userService.getUser(UserGetRequest.builder()
                .userName(userName)
                .build());

        if (user == null) {
            return ApiResult.ofFail(ApiErrorEnum.LOGIN_USER_NOT_EXIST);
        }

        // 解密
        String decodePwd = decryptPwd(user.getPwdSalt(), user.getUserPwd());
        if (!StringUtils.equals(password, decodePwd)) {
            return ApiResult.ofFail(ApiErrorEnum.LOGIN_PWD_ERROR);
        }

        UserView userView = UserView.convert(user);
        result.put(DATA, userView);
        return result;
    }

    public Map<String, Object> doRegister(String userName, String password) {
        Map<String, Object> result = new HashMap<>();
        //
        // 简单验证


        //
        return result;
    }
}
