package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.COOKIE_SESSION_ID;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happy.chat.helper.ChatApiHelper;

@RestController
@RequestMapping("/rest/h/chat")
public class ChatController {
    @Autowired
    private ChatApiHelper chatApiHelper;

    @RequestMapping("/sayHi")
    public Map<String, Object> sayHi(@RequestParam("robotId") String robotId) {
        // 返回sayHi的随机文案
        return chatApiHelper.getIceBreakWords(robotId);
    }

    @RequestMapping("/request")
    public Map<String, Object> request(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                       @RequestParam("robotId") String robotId,
                                       @RequestParam("content") String content) {
        return chatApiHelper.request(userId, robotId, content);
    }

    // chat列表 只展示最新一条消息
    @RequestMapping("/list")
    public Map<String, Object> list(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId) {
        return chatApiHelper.listUserChat(userId);
    }

    // 和robot的历史聊天信息
    @RequestMapping("/robot/history")
    public Map<String, Object> historyChat(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                           @RequestParam("robotId") String robotId) {
        return chatApiHelper.getUserRobotHistoryChats(userId, robotId);
    }
}
