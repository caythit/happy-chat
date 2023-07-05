package com.happy.chat.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/h/robot")
public class RobotController {
    @RequestMapping("/profile")
    public Map<String, Object> profile(@RequestParam("userName") String userName,
                                       @RequestParam("password") String password) {
        Map<String, Object> result = new HashMap<String, Object>();
        return result;
    }
}
