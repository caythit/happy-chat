package com.happy.chat.interceptor;

import static com.happy.chat.constants.Constant.COOKIE_SESSION_ID;

import java.util.Arrays;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.happy.chat.annotation.LoginRequired;

public class LoginCheckInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod h = (HandlerMethod) handler;
        LoginRequired loginRequired = h.getMethodAnnotation(LoginRequired.class);
        if (loginRequired == null) {
            return true;
        }

        Cookie[] cookies = request.getCookies();
        String userId =
                Arrays.stream(cookies)
                        .filter(c -> c.getName().equals(COOKIE_SESSION_ID))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse("");
        if (StringUtils.isEmpty(userId)) {
            // todo
            return false;
        }
        String dummyUid = request.getParameter("ud");
        // 拿出cookie的userId和请求的dummyUid 做对比
        if (!StringUtils.equals(userId, dummyUid)) { // 登录了 需要和ud做比较
            // todo
            return false;
        }
        return true;
    }
}
