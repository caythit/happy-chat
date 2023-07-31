package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.DATA;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happy.chat.helper.StartupApiHelper;
import com.happy.chat.uitls.ApiResult;

@RestController
@RequestMapping("/rest/h/startup")
public class StartupController {

    @Autowired
    private StartupApiHelper startupApiHelper;

    /**
     * 兴趣选择
     * @return
     */
    @PostMapping("/selectPrefer")
    public Map<String, Object> selectPrefer(@RequestParam(value = "ud") String dummyUid,
                                            @RequestParam(value = "robotPrefer", defaultValue = "") String preferRobotId,
                                            @RequestParam(value = "agePrefer", defaultValue = "") String agePrefer) {
        return startupApiHelper.recordUserPrefer(dummyUid, preferRobotId, agePrefer);
    }

    /**
     * 下发启动配置相关
     *
     * @return
     */
    @RequestMapping("/config")
    public Map<String, Object> config(HttpServletResponse response) {
        Map<String, Object> res = ApiResult.ofSuccess();
        // 返回相关配置
        res.put(DATA, startupApiHelper.getConfig());

//        // 成功设置cookie
//        if (res.get(ERROR_CODE).equals(ErrorEnum.SUCCESS.getErrCode())) {
//            // 创建一个 cookie对象
//            Cookie cookie = new Cookie(COOKIE_SESSION_ID, user.getUserId());
//            cookie.setSecure(true);  //Https 安全cookie
//            cookie.setMaxAge(365 * 24 * 60 * 60);
//            //将cookie对象加入response响应
//            response.addCookie(cookie);
//        }
        return res;
    }
}
