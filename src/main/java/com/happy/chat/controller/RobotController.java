package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.COOKIE_SESSION_ID;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happy.chat.helper.RobotApiHelper;

@RestController
@RequestMapping("/rest/h/robot")
public class RobotController {

    @Autowired
    private RobotApiHelper robotApiHelper;

    /**
     * 查看AI信息
     *
     * @param robotId
     * @return
     */
    @RequestMapping("/profile")
    public Map<String, Object> profile(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                       @RequestParam("robotId") String robotId) {
        return robotApiHelper.getRobotProfile(userId, robotId);
    }
}
