package com.happy.chat.interceptor;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Lazy
public class SignInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private UserService userService;
//
//    /**
//     * 预处理回调方法，实现处理器的预处理
//     * 返回值：true表示继续流程；false表示流程中断，不会继续调用其他的拦截器或处理器
//     */
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
//            throws Exception {
//        Cookie[] cookies = request.getCookies();
//        String session = null;
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if (cookie.getName().equals(COOKIE_SESSION_ID)) {
//                    session = cookie.getValue();
//                }
//            }
//        }
//        // 判断sessionId是否有效
//        User tUser = userService.checkSession(session);
//        if (tUser == null) {
//            response.setCharacterEncoding("UTF-8");
//            response.setContentType("application/json;charset=UTF-8");
//
//            response.getWriter().print(ObjectMapperUtils.toJSON(ApiResult.ofFail(ErrorEnum.OPERATION_NEED_LOGIN)));
//            return false;
//        }
//        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(tUser, null);
//        authentication.setDetails(request);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        return true;
//    }
}