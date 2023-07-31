package com.happy.chat.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
    public Map<String, Object> profile(@RequestParam("robotId") String robotId) {
        return robotApiHelper.getRobotProfile(robotId);
    }
}
