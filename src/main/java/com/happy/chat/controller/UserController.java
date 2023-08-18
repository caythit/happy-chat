package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.COOKIE_SESSION_ID;
import static com.happy.chat.constants.Constant.DATA;
import static com.happy.chat.constants.Constant.ERROR_CODE;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happy.chat.annotation.LoginRequired;
import com.happy.chat.domain.User;
import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.helper.UserApiHelper;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.view.UserInfoView;

import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rest/h/user")
@Slf4j
public class UserController {

    private final String prometheusName = "user_api";
    private final String prometheusHelp = "user for grafana";

    @Autowired
    private CollectorRegistry userRegistry;

    @Autowired
    private UserApiHelper useHelper;

    @RequestMapping("/loginByEmail")
    public Map<String, Object> loginByEmail(HttpServletResponse response,
                                            @RequestParam("email") String email,
                                            @RequestParam("password") String password) {
        Map<String, Object> res = useHelper.doLoginByEmail(email, password);
        // 成功设置cookie
        if (res.get(ERROR_CODE).equals(ErrorEnum.SUCCESS.getErrCode())) {
            UserInfoView user = (UserInfoView) res.get(DATA);
            // 创建一个 cookie对象
            Cookie cookie = new Cookie(COOKIE_SESSION_ID, user.getUserId());
//            cookie.setSecure(true);  //Https 安全cookie
            cookie.setMaxAge(365 * 24 * 60 * 60); // 30天过期
            //将cookie对象加入response响应
            response.addCookie(cookie);
        }
        return res;
    }

    @RequestMapping("/registerByEmail")
    public Map<String, Object> registerByEmail(HttpServletResponse response,
                                               @RequestParam(value = "ud") String dummyUid,
                                               @RequestParam("email") String email,
                                               @RequestParam("password") String password) {
        Map<String, Object> res = useHelper.doRegisterByEmail(dummyUid, email, password);
        // 成功设置cookie
        if (res.get(ERROR_CODE).equals(ErrorEnum.SUCCESS.getErrCode())) {
            UserInfoView user = (UserInfoView) res.get(DATA);
            // 创建一个 cookie对象
            Cookie cookie = new Cookie(COOKIE_SESSION_ID, user.getUserId());
//            cookie.setSecure(true);  //Https 安全cookie
            cookie.setMaxAge(365 * 24 * 60 * 60); // 30天过期
            //将cookie对象加入response响应
            response.addCookie(cookie);
        }
        return res;
    }

    @RequestMapping("/modifyUserName")
    public Map<String, Object> modifyUserName(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                              @RequestParam("userName") String userName) {
        log.info("logout, userId={}, userName={}", userId, userName);
        return useHelper.modifyUserName(userId, userName);
    }

    @LoginRequired
    @RequestMapping("/logout")
    public Map<String, Object> logout(HttpServletResponse response,
                                      @CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                      @RequestParam(value = "ud") String dummyUid) {
        log.info("logout, userId={}, dummyUserId={}", userId, dummyUid);
        // 将Cookie的值设置为null
        Cookie cookie = new Cookie(COOKIE_SESSION_ID, null);
        //将`Max-Age`设置为0
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ApiResult.ofSuccess();
    }

    // 没有用了。
    @RequestMapping("/profile")
    public Map<String, Object> profile() {
        return ApiResult.ofSuccess();
    }

    @LoginRequired
    @RequestMapping("/modifyPassword")
    public Map<String, Object> modifyPassword(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                              @RequestParam(value = "ud") String dummyUid,
                                              @RequestParam("oldPwd") String oldPwd,
                                              @RequestParam("newPwd") String newPwd) {
        // todo 是不是放在interceptor做
        if (StringUtils.isNotEmpty(userId) && !StringUtils.equals(userId, dummyUid)) { // 登录了 需要和ud做比较
            return ApiResult.ofFail(ErrorEnum.UD_NOT_MATCHED);
        }

        // 检查旧密码是否正确
        ErrorEnum apiErrorEnum = useHelper.checkPassword(userId, oldPwd, "modifyPassword");
        if (apiErrorEnum != ErrorEnum.SUCCESS) {
            return ApiResult.ofFail(apiErrorEnum);
        }
        // 校验新密码格式 同时重置密码
        return useHelper.resetPassword(userId, newPwd, "modifyPassword");
    }

}
