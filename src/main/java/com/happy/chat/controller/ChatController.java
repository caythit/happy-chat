package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.COOKIE_SESSION_ID;
import static com.happy.chat.constants.Constant.DATA;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happy.chat.helper.ChatApiHelper;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.view.ChatHistoryView;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rest/h/chat")
@Slf4j
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
                                       @RequestParam(value = "ud", required = false) String dummyUid,
                                       @RequestParam("robotId") String robotId,
                                       @RequestParam("content") String content) {
        log.info("request = {} {} {} {}", userId, dummyUid, robotId, content);
        // todo 改成cookie userId
        return chatApiHelper.request(dummyUid, robotId, content);
    }

    // chat列表 只展示最新一条消息
    @RequestMapping("/list")
    public Map<String, Object> list(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                    @RequestParam(value = "ud", required = false) String dummyUid) {
        // todo 改成cookie userId
        return chatApiHelper.listUserChat(dummyUid);
    }

    // 和robot的历史聊天信息
    @RequestMapping("/robot/history")
    public Map<String, Object> historyChat(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                           @RequestParam(value = "ud", required = false) String dummyUid,
                                           @RequestParam("robotId") String robotId) {
        // todo 改成cookie userId
        ChatHistoryView chatHistoryView = chatApiHelper.getUserRobotHistoryChats(dummyUid, robotId);
        Map<String, Object> result = ApiResult.ofSuccess();
        result.put(DATA, chatHistoryView);
        return result;
    }
}
