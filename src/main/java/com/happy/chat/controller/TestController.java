package com.happy.chat.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/h/test")
public class TestController {
    @RequestMapping("/hello")
    public Map<String, Object> hello(@RequestParam("userName") String userName) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("data", String.format("hello, %s", userName));
        return result;
    }
}
