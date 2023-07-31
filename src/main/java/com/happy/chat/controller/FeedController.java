package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.COOKIE_SESSION_ID;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happy.chat.helper.FeedApiHelper;

@RestController
@RequestMapping("/rest/h/feed")
public class FeedController {

    @Autowired
    private FeedApiHelper feedApiHelper;

    /**
     * 双列页面，展示pyml
     *
     * @return
     */
    @RequestMapping("/foryou")
    public Map<String, Object> foryou(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                      @RequestParam(value = "size", defaultValue = "100") int size) {
        return feedApiHelper.foryou(userId, size);
    }
}
