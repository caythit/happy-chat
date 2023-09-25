package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.PERF_SETTING_MODULE;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.helper.EmailHelper;
import com.happy.chat.helper.SettingApiHelper;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.PrometheusUtils;

// 第一步 输入邮箱，发送邮箱校验码，检查：邮箱是否已被使用，没使用则提示；邮箱格式验证
// 第二步 验证邮箱校验码
// 第三步 输入新密码，校验格式

@RestController
@RequestMapping("/rest/h/forgot/password")
public class ForgotPasswordController {
    @Autowired
    private SettingApiHelper settingApiHelper;

    @Autowired
    private EmailHelper emailHelper;

    @Autowired
    private PrometheusUtils prometheusUtil;

    // 第一步 输入邮箱，发送邮箱校验码，检查：邮箱是否已被使用，没使用则提示；邮箱格式验证
    @RequestMapping("/sendEmailCode")
    public Map<String, Object> send(@RequestParam("email") String email) {
        prometheusUtil.perf(PERF_SETTING_MODULE, "忘记密码第1步-发送验证码API入口");
        ErrorEnum errorEnum = emailHelper.sendCode(email, "Verify your email address",
                true, "forgotpwd");
        if (errorEnum == ErrorEnum.SUCCESS) {
            prometheusUtil.perf(PERF_SETTING_MODULE, "忘记密码第1步-发送验证码成功");
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
        prometheusUtil.perf(PERF_SETTING_MODULE, "忘记密码第2步-校验验证码API入口");

        ErrorEnum errorEnum = emailHelper.verifyCode(email, emailVerifyCode, "forgotPassword");
        if (errorEnum == ErrorEnum.SUCCESS) {
            prometheusUtil.perf(PERF_SETTING_MODULE, "忘记密码第2步-校验验证码成功");
            return ApiResult.ofSuccess();
        }
        return ApiResult.ofFail(errorEnum);
    }

    // 重置密码，没登录
    @PostMapping("/reset")
    public Map<String, Object> reset(@RequestParam("email") String email,
                                     @RequestParam("newPwd") String pwd) {
        prometheusUtil.perf(PERF_SETTING_MODULE, "忘记密码第3步-重置密码API入口");

        ErrorEnum errorEnum = settingApiHelper.forgotResetPassword(email, pwd);
        if (errorEnum == ErrorEnum.SUCCESS) {
            prometheusUtil.perf(PERF_SETTING_MODULE, "忘记密码第3步-重置密码成功");
            return ApiResult.ofSuccess();
        }
        return ApiResult.ofFail(errorEnum);
    }


}
