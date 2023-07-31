package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.COOKIE_SESSION_ID;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

// 第一步输入密码后校验是否正确，查db。
// 第二步发送邮箱校验码，邮箱格式验证
// 第三步验证邮箱校验码

@RestController
@RequestMapping("/rest/h/email/modify")
public class EmailModifyController {

    @Autowired
    private UserApiHelper userApiHelper;

    @Autowired
    private EmailHelper emailHelper;

    // 第1步：输入密码后校验是否正确
    @LoginRequired
    @RequestMapping("/check/password")
    public Map<String, Object> passwordCheck(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                             @RequestParam("ud") String dummyUid,
                                             @RequestParam("password") String password) {

        if (StringUtils.isNotEmpty(userId) && !StringUtils.equals(userId, dummyUid)) { // 登录了 需要和ud做比较
            return ApiResult.ofFail(ErrorEnum.UD_NOT_MATCHED);
        }
        // 检查旧密码是否正确
        ErrorEnum apiErrorEnum = userApiHelper.checkPassword(userId, password, "modifyEmail");
        if (apiErrorEnum != ErrorEnum.SUCCESS) {
            return ApiResult.ofFail(apiErrorEnum);
        }
        return ApiResult.ofSuccess();
    }

    // 第2步：邮箱格式验证，发送验证码
    @LoginRequired
    @RequestMapping("/sendEmailCode")
    public Map<String, Object> send(@RequestParam("email") String email) {
        // 验证邮箱 todo 确认subject和text
        ErrorEnum errorEnum = emailHelper.sendCode(email, "", "", false, "modifyEmail");
        if (errorEnum == ErrorEnum.SUCCESS) {
            return ApiResult.ofSuccess();
        }
        return ApiResult.ofFail(errorEnum);
    }

    // 第3步：验证码验证
    @LoginRequired
    @PostMapping("/verifyEmailCode")
    public Map<String, Object> verifyEmailCode(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                               @RequestParam("email") String email,
                                               @RequestParam("emailVerifyCode") String emailVerifyCode) {
        ErrorEnum errorEnum = emailHelper.verifyCode(email, emailVerifyCode, "modifyEmail");
        if (errorEnum == ErrorEnum.SUCCESS) {
            return userApiHelper.rebindEmail(userId, email);
        }
        return ApiResult.ofFail(errorEnum);
    }
}
