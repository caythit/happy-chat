package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.COOKIE_SESSION_ID;
import static com.happy.chat.constants.Constant.PERF_ERROR_MODULE;
import static com.happy.chat.constants.Constant.PERF_SETTING_MODULE;

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
import com.happy.chat.helper.SettingApiHelper;
import com.happy.chat.helper.UserApiHelper;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.PrometheusUtils;

// 第一步输入密码后校验是否正确，查db。
// 第二步发送邮箱校验码，邮箱格式验证
// 第三步验证邮箱校验码

@RestController
@RequestMapping("/rest/h/email/modify")
public class EmailModifyController {

    @Autowired
    private SettingApiHelper settingApiHelper;

    @Autowired
    private UserApiHelper userApiHelper;

    @Autowired
    private EmailHelper emailHelper;

    @Autowired
    private PrometheusUtils prometheusUtil;

    // 第1步：输入密码后校验是否正确
    @LoginRequired
    @RequestMapping("/check/password")
    public Map<String, Object> passwordCheck(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                             @RequestParam("ud") String dummyUid,
                                             @RequestParam("password") String password) {

        prometheusUtil.perf(PERF_SETTING_MODULE, "修改绑定邮箱第1步-密码校验API入口");

        if (StringUtils.isNotEmpty(userId) && !StringUtils.equals(userId, dummyUid)) { // 登录了 需要和ud做比较
            prometheusUtil.perf(PERF_ERROR_MODULE, "修改绑定邮箱第1步失败-userId和dummyUid不匹配");
            return ApiResult.ofFail(ErrorEnum.UD_NOT_MATCHED);
        }
        // 检查旧密码是否正确
        ErrorEnum apiErrorEnum = userApiHelper.checkPassword(userId, password, "modify_email");
        if (apiErrorEnum == ErrorEnum.SUCCESS) {
            prometheusUtil.perf(PERF_SETTING_MODULE, "修改绑定邮箱第1步-密码校验成功");
            return ApiResult.ofSuccess();
        }
        return ApiResult.ofFail(apiErrorEnum);
    }

    // 第2步：邮箱格式验证，发送验证码
    @LoginRequired
    @RequestMapping("/sendEmailCode")
    public Map<String, Object> send(@RequestParam("email") String email) {
        prometheusUtil.perf(PERF_SETTING_MODULE, "修改绑定邮箱第2步-发送验证码API入口");

        // 验证邮箱
        ErrorEnum errorEnum = emailHelper.sendCode(email, "Verify your email address", false, "bindemail");
        if (errorEnum == ErrorEnum.SUCCESS) {
            prometheusUtil.perf(PERF_SETTING_MODULE, "修改绑定邮箱第2步-发送验证码成功");
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
        prometheusUtil.perf(PERF_SETTING_MODULE, "修改绑定邮箱第3步-校验验证码API入口");

        ErrorEnum errorEnum = emailHelper.verifyCode(email, emailVerifyCode, "modifyEmail");
        if (errorEnum == ErrorEnum.SUCCESS) {
            prometheusUtil.perf(PERF_SETTING_MODULE, "修改绑定邮箱第3步-校验验证码成功");
            errorEnum = settingApiHelper.rebindEmail(userId, email);
            if (errorEnum == ErrorEnum.SUCCESS) {
                prometheusUtil.perf(PERF_SETTING_MODULE, "修改绑定邮箱第3步-修改成功");
                return ApiResult.ofSuccess();
            }
        }
        return ApiResult.ofFail(errorEnum);
    }
}
