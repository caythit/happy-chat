package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.COOKIE_SESSION_ID;
import static com.happy.chat.constants.Constant.PERF_FEED_MODULE;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happy.chat.helper.FeedApiHelper;
import com.happy.chat.uitls.PrometheusUtils;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rest/h/feed")
@Slf4j
public class FeedController {

    @Autowired
    private FeedApiHelper feedApiHelper;

    @Autowired
    private PrometheusUtils prometheusUtil;

    /**
     * 双列页面，展示pyml
     *
     * @return
     */
    @RequestMapping("/foryou")
    public Map<String, Object> foryou(@CookieValue(value = COOKIE_SESSION_ID, defaultValue = "") String userId,
                                      @RequestParam(value = "ud") String dummyUid,
                                      @RequestParam(value = "size", required = false, defaultValue = "100") int size) {
        log.info("foryou {} {}", dummyUid, userId);
        prometheusUtil.perf(PERF_FEED_MODULE, "feed API入口");
        return feedApiHelper.foryou(userId, dummyUid, size);
    }
}
