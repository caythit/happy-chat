package com.happy.chat.controller;

import static com.happy.chat.uitls.CacheKeyProvider.chatSensitiveWordKey;
import static com.happy.chat.uitls.CacheKeyProvider.defaultRobotRespChatKey;
import static com.happy.chat.uitls.CacheKeyProvider.robotGptPromptKey;
import static com.happy.chat.uitls.CacheKeyProvider.startupConfigKey;
import static com.happy.chat.uitls.CacheKeyProvider.userChatgptWarnMaxCountKey;
import static com.happy.chat.uitls.CacheKeyProvider.userEnterChatgptAdvanceModelThresholdKey;
import static com.happy.chat.uitls.CacheKeyProvider.userExitHappyModelExpireMillsKey;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.happy.chat.domain.Robot;
import com.happy.chat.model.StartupConfigModel;
import com.happy.chat.service.RobotService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.ObjectMapperUtils;
import com.happy.chat.uitls.PrometheusUtils;
import com.happy.chat.uitls.RedisUtil;
import com.happy.chat.view.StartupConfigView;

import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rest/h/test")
@Slf4j
public class TestController {
    private final String prometheusName = "test";
    private final String prometheusHelp = "testApi";

    @Autowired
    private CollectorRegistry testRegistry;

    @Autowired
    private RobotService robotService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PrometheusUtils prometheusUtil;


    @RequestMapping("/test")
    public Map<String, Object> test(@RequestParam("userName") String userName) {
        Map<String, Object> result = ApiResult.ofSuccess();


        prometheusUtil.perf("testApi_recall");
        prometheusUtil.perf("chatApi_recall");

        result.put("data", String.format("hello, %s", userName));
        result.put("prompt", redisUtil.get(robotGptPromptKey("rb_XXXX5", "advanced")));

        log.info("test log...");
        log.warn("test log...");
        log.error("test log...");
        return result;
    }

    @RequestMapping("/insertStartupConfig")
    public Map<String, Object> insertStartupConfig() {
        Map<String, Object> result = ApiResult.ofSuccess();

        return result;
    }

    @RequestMapping("/insertPrepareData")
    public Map<String, Object> insertPrepareData() {
        Map<String, Object> result = ApiResult.ofSuccess();

        // 敏感词
        redisUtil.rightPushAll(chatSensitiveWordKey(), "are LLM", "are robot", "are chat gpt");
        result.put(chatSensitiveWordKey(), redisUtil.range(chatSensitiveWordKey(), 0, -1));

        // 默认回复
        redisUtil.rightPushAll(defaultRobotRespChatKey(), "It's not funny, you think I'm boring like a robot?");
        result.put(defaultRobotRespChatKey(), redisUtil.range(defaultRobotRespChatKey(), 0, -1));

        // 退场机制时间
        redisUtil.rightPushAll(userExitHappyModelExpireMillsKey(), "1800000");
        result.put(userExitHappyModelExpireMillsKey(), redisUtil.get(userExitHappyModelExpireMillsKey()));

        // 聊天请求热情版gpt触发消息条数
        redisUtil.rightPushAll(userEnterChatgptAdvanceModelThresholdKey(), "10");
        result.put(userEnterChatgptAdvanceModelThresholdKey(), redisUtil.get(userEnterChatgptAdvanceModelThresholdKey()));

        // 聊天普通版gpt警报阈值次数
        redisUtil.rightPushAll(userChatgptWarnMaxCountKey(), "3");
        result.put(userChatgptWarnMaxCountKey(), redisUtil.get(userChatgptWarnMaxCountKey()));

        StartupConfigModel startupConfig = new StartupConfigModel();
        startupConfig.setLogoUrl("https://www.freepnglogos.com/uploads/spotify-logo-png/spotify-icon-marilyn-scott-0.png");
        startupConfig.setAgeOptions(ImmutableList.of("18-25", "25-35", "35+"));
        startupConfig.setIntroduceText("Hello!\n who would you like\n to chat with?");
        startupConfig.setWelcomeText("How are you \ntoday?");
        startupConfig.setPymlRobotIds(ImmutableSet.<String>builder()
                .add("rb_test8")
                .add("rb_test1")
                .add("rb_test3")
                .add("rb_test5")
                .add("rb_test10")
                .add("rb_test2")
                .build());

        redisUtil.set(startupConfigKey(), ObjectMapperUtils.toJSON(startupConfig));
        StartupConfigModel model = ObjectMapperUtils.fromJSON(redisUtil.get(startupConfigKey()), StartupConfigModel.class);
        if (model != null) {
            StartupConfigView startupConfigView = new StartupConfigView();
            startupConfigView.setLogoUrl(model.getLogoUrl());
            startupConfigView.setAgeOptions(model.getAgeOptions());
            startupConfigView.setIntroduceText(model.getIntroduceText());
            startupConfigView.setWelcomeText(model.getWelcomeText());
            Map<String, Robot> robotMap = robotService.batchGetRobotById(model.getPymlRobotIds());
            startupConfigView.setPymlRobots(robotMap.values().stream()
                    .map(r -> new StartupConfigView.RobotStartupView(r.getRobotId(), r.getHeadUrl(), r.getName()))
                    .collect(Collectors.toList()));
            result.put("startup", ObjectMapperUtils.toJSON(startupConfigView));
        }


//        // 促支付文案
//        redisUtil.rightPushAll(chatUnPayWordKey(), "ai", "chatgpt");
//        result.put(DATA, redisUtil.range(defaultRobotRespChatKey(), 0, -1));
//
//        // 支付完成文案
//        redisUtil.rightPushAll(chatFinishPayWordKey(), "ai", "chatgpt");
//        result.put(DATA, redisUtil.range(chatFinishPayWordKey(), 0, -1));


        return result;
    }
}