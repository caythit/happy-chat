package com.happy.chat.controller;

import static com.happy.chat.uitls.CacheKeyProvider.chatFinishPayTipsKey;
import static com.happy.chat.uitls.CacheKeyProvider.chatSensitiveWordKey;
import static com.happy.chat.uitls.CacheKeyProvider.chatSystemTipsKey;
import static com.happy.chat.uitls.CacheKeyProvider.chatUnPayTipsKey;
import static com.happy.chat.uitls.CacheKeyProvider.chatWarnWordKey;
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
import com.happy.chat.uitls.FileUtils;
import com.happy.chat.uitls.ObjectMapperUtils;
import com.happy.chat.uitls.PrometheusUtils;
import com.happy.chat.uitls.RedisUtil;
import com.happy.chat.view.StartupConfigView;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rest/h/test")
@Slf4j
public class TestController {

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

    @RequestMapping("/insertPrepareData")
    public Map<String, Object> insertPrepareData() {
        Map<String, Object> result = ApiResult.ofSuccess();

        // 敏感词
        redisUtil.delete(chatSensitiveWordKey());
        redisUtil.rightPushAll(chatSensitiveWordKey(), "are LLM", "are robot", "are chat gpt");
        result.put(chatSensitiveWordKey(), redisUtil.range(chatSensitiveWordKey(), 0, -1));

        // 敏感词
        redisUtil.delete(chatWarnWordKey());
        redisUtil.rightPushAll(chatWarnWordKey(), "are LLM", "are robot", "are chat gpt");
        result.put(chatWarnWordKey(), redisUtil.range(chatWarnWordKey(), 0, -1));

        // 默认回复
        redisUtil.delete(defaultRobotRespChatKey());
        redisUtil.rightPushAll(defaultRobotRespChatKey(), "It's not funny, you think I'm boring like a robot?");
        result.put(defaultRobotRespChatKey(), redisUtil.range(defaultRobotRespChatKey(), 0, -1));

        // 系统提示 规劝文案
        redisUtil.set(chatSystemTipsKey(), "Don't scare a girl. Do it gently and softly.");
        result.put(chatSystemTipsKey(), redisUtil.get(chatSystemTipsKey()));


        // 促支付文案
        redisUtil.rightPushAll(chatUnPayTipsKey(), "Don’t be shy to undress me, only $9.9",
                "Darling i want to serve you better, only $9.9", "$9.9 for R-18 experience.", "I'll show you my true collor, only $9.9",
                "Trying to get to second base with me? Only $9.9", "You want me to give you a tease? Only $9.9");
        result.put(chatUnPayTipsKey(), redisUtil.range(chatUnPayTipsKey(), 0, -1));

        // 支付完成文案
        redisUtil.rightPushAll(chatFinishPayTipsKey(), "I will only serve you at all time.", "I want you so bad.",
                "I am thirsty to have you.", "I love you. Are you rich in pants?", "Let's have a short time but good time",
                "Give me a spanking now!", "I'm so turned on!");
        result.put(chatFinishPayTipsKey(), redisUtil.range(chatFinishPayTipsKey(), 0, -1));


        // 退场机制时间
        redisUtil.set(userExitHappyModelExpireMillsKey(), "1800000");
        result.put(userExitHappyModelExpireMillsKey(), redisUtil.get(userExitHappyModelExpireMillsKey()));

        // 聊天请求热情版gpt触发消息条数
        redisUtil.set(userEnterChatgptAdvanceModelThresholdKey(), "10");
        result.put(userEnterChatgptAdvanceModelThresholdKey(), redisUtil.get(userEnterChatgptAdvanceModelThresholdKey()));

        // 聊天普通版gpt警报阈值次数
        redisUtil.set(userChatgptWarnMaxCountKey(), "3");
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

        return result;
    }


    public static void main(String[] args) {
        String fileName = String.format("%s_%s.prompt", "rb_test11", "normal");

        System.out.println(FileUtils.getFileContent(fileName));
    }
}