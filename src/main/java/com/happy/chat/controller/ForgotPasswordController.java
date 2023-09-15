package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.COOKIE_SESSION_ID;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happy.chat.annotation.LoginRequired;
import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.helper.EmailHelper;
import com.happy.chat.helper.UserApiHelper;
import com.happy.chat.uitls.ApiResult;

// 第一步 输入邮箱，发送邮箱校验码，检查：邮箱是否已被使用，没使用则提示；邮箱格式验证
// 第二步 验证邮箱校验码
// 第三步 输入新密码，校验格式

@RestController
@RequestMapping("/rest/h/forgot/password")
public class ForgotPasswordController {
    @Autowired
    private UserApiHelper userApiHelper;

    @Autowired
    private EmailHelper emailHelper;

    // 第一步 输入邮箱，发送邮箱校验码，检查：邮箱是否已被使用，没使用则提示；邮箱格式验证
    @RequestMapping("/sendEmailCode")
    public Map<String, Object> send(@RequestParam("email") String email) {
        // 验证邮箱 todo 确认subject和text
        ErrorEnum errorEnum = emailHelper.sendCode(email, "Verify your email address",
                true, "forgot password");
        if (errorEnum == ErrorEnum.SUCCESS) {
            return ApiResult.ofSuccess();
        }
        return ApiResult.ofFail(errorEnum);
    }

//    @RequestMapping("/check/email")
//    public Map<String, Object> checkEmail(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
//                                          @RequestParam("email") String email) {
//        // check email 格式
//        boolean emailValid = CommonUtils.emailPatternValid(email);
//        if (!emailValid) {
//            return ApiResult.ofFail(ApiErrorEnum.EMAIL_PATTERN_INVALID);
//        }
//        // check email 是否被使用，必须是使用状态
//        boolean result = userApiHelper.checkEmailUsed(email);
//        if (result) {
//            return ApiResult.ofSuccess();
//        }
//        return ApiResult.ofFail(ApiErrorEnum.EMAIL_NOT_EXIST);
//    }

    // 验证码验证
    @PostMapping("/verifyEmailCode")
    public Map<String, Object> verifyEmailCode(@RequestParam("email") String email,
                                               @RequestParam("emailVerifyCode") String emailVerifyCode) {
        ErrorEnum errorEnum = emailHelper.verifyCode(email, emailVerifyCode, "forgotPassword");
        if (errorEnum == ErrorEnum.SUCCESS) {
            return ApiResult.ofSuccess();
        }
        return ApiResult.ofFail(errorEnum);
    }

    // 重置密码，没登录
    @PostMapping("/reset")
    public Map<String, Object> reset(@RequestParam("email") String email,
                                     @RequestParam("newPwd") String pwd) {
        return userApiHelper.forgotResetPassword(email, pwd);
    }


}
