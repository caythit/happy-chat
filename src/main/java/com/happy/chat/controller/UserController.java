package com.happy.chat.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happy.chat.helper.UseApiHelper;

@RestController
@RequestMapping("/rest/h/user")
public class UserController {
    @Autowired
    private UseApiHelper useHelper;

    @RequestMapping("/login")
    public Map<String, Object> login(@RequestParam("userName") String userName,
                                     @RequestParam("password") String password) {
        return useHelper.doLogin(userName, password);
    }

    @RequestMapping("/register")
    public Map<String, Object> register(@RequestParam("userName") String userName,
                                        @RequestParam("password") String password) {
        return useHelper.doRegister(userName, password);
    }

    @RequestMapping("/logout")
    public Map<String, Object> logout(@RequestParam("userName") String userName,
                                      @RequestParam("password") String password) {
        Map<String, Object> result = new HashMap<String, Object>();
        return result;
    }

    @RequestMapping("/modifyPassword")
    public Map<String, Object> modifyPassword(@RequestParam("userName") String userName,
                                              @RequestParam("password") String password) {
        Map<String, Object> result = new HashMap<String, Object>();
        return result;
    }

    @RequestMapping("/profile")
    public Map<String, Object> profile(@RequestParam("userName") String userName,
                                       @RequestParam("password") String password) {
        Map<String, Object> result = new HashMap<String, Object>();
        return result;
    }
}
