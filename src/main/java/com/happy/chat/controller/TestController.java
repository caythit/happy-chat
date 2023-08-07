package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.DATA;
import static com.happy.chat.constants.Constant.EXTRA_INFO_ROBOT_ABOUT_ME;
import static com.happy.chat.constants.Constant.EXTRA_INFO_ROBOT_INTEREST;
import static com.happy.chat.constants.Constant.EXTRA_INFO_ROBOT_WORK;
import static com.happy.chat.constants.Constant.ROBOT_ID_PREFIX;
import static com.happy.chat.uitls.CacheKeyProvider.chatFinishPayWordKey;
import static com.happy.chat.uitls.CacheKeyProvider.chatSensitiveWordKey;
import static com.happy.chat.uitls.CacheKeyProvider.chatUnPayWordKey;
import static com.happy.chat.uitls.CacheKeyProvider.defaultRobotRespChatKey;
import static com.happy.chat.uitls.CacheKeyProvider.startupConfigKey;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.happy.chat.domain.Robot;
import com.happy.chat.service.RobotService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.CommonUtils;
import com.happy.chat.uitls.ObjectMapperUtils;
import com.happy.chat.uitls.PrometheusUtils;
import com.happy.chat.uitls.RedisUtil;
import com.happy.chat.view.RobotInfoView;
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
        log.info("test log...");
        return result;
    }

    @RequestMapping("/insertStartupConfig")
    public Map<String, Object> insertStartupConfig() {
        log.info("test log ingo insertStartupConfig");
        log.warn("test log warn insertStartupConfig");
        log.error("test log error insertStartupConfig");
        Map<String, Object> result = ApiResult.ofSuccess();
        StartupConfigView startupConfigView = new StartupConfigView();
        startupConfigView.setLogoUrl("https://testlogo");
        startupConfigView.setAgeOptions(ImmutableList.of("18-25", "25-35", "35以上"));
        startupConfigView.setIntroduceText("Hello!\n who would you like\n to chat with?");
        startupConfigView.setWelcomeText("How are you \ntoday?");
        startupConfigView.setPymlRobots(ImmutableList.<StartupConfigView.RobotStartupView>builder()
                .add(new StartupConfigView.RobotStartupView("1", "head1", "liu1"))
                .add(new StartupConfigView.RobotStartupView("2", "head2", "liu2"))
                .add(new StartupConfigView.RobotStartupView("3", "head3", "liu3"))
                .add(new StartupConfigView.RobotStartupView("4", "head4", "liu4"))
                .add(new StartupConfigView.RobotStartupView("5", "head5", "liu5"))
                .add(new StartupConfigView.RobotStartupView("6", "head6", "liu6"))
                .build());

        redisUtil.set(startupConfigKey(), ObjectMapperUtils.toJSON(startupConfigView));
        result.put(DATA, ObjectMapperUtils.fromJSON(redisUtil.get(startupConfigKey()), StartupConfigView.class));
        return result;
    }

    @RequestMapping("/insertChatPrepareData")
    public Map<String, Object> insertChatSensitiveWord() {
        Map<String, Object> result = ApiResult.ofSuccess();

        // 敏感词
        redisUtil.rightPushAll(chatSensitiveWordKey(), "ai", "chatgpt");
        result.put("sensitive", redisUtil.range(chatSensitiveWordKey(), 0, -1));

        // 默认回复
        redisUtil.rightPushAll(defaultRobotRespChatKey(), "ai", "chatgpt");
        result.put(DATA, redisUtil.range(defaultRobotRespChatKey(), 0, -1));

        // 促支付文案
        redisUtil.rightPushAll(chatUnPayWordKey(), "ai", "chatgpt");
        result.put(DATA, redisUtil.range(defaultRobotRespChatKey(), 0, -1));

        // 支付完成文案
        redisUtil.rightPushAll(chatFinishPayWordKey(), "ai", "chatgpt");
        result.put(DATA, redisUtil.range(chatFinishPayWordKey(), 0, -1));

        return result;
    }

    @RequestMapping("/insertRobot")
    public Map<String, Object> insertRobot(@RequestParam("name") String userName,
                                           @RequestParam("sex") int sex,
                                           @RequestParam("age") int age,
                                           @RequestParam("city") String city,
                                           @RequestParam("country") String country,
                                           @RequestParam("headUrl") String headUrl,
                                           @RequestParam("coverUrl") String coverUrl,
                                           @RequestParam("bgUrl") String bgUrl) {
        Map<String, Object> result = new HashMap<String, Object>();
        Robot robot = new Robot();
        robot.setRobotId(CommonUtils.uuid(ROBOT_ID_PREFIX));
        robot.setName(userName);
        robot.setSex(sex);
        robot.setAge(age);
        robot.setCity(city);
        robot.setCountry(country);
        robot.setHeadUrl(headUrl);
        robot.setCoverUrl(coverUrl);
        robot.setBgUrl(bgUrl);
        robot.setExtraInfo(ObjectMapperUtils.toJSON(ImmutableMap.of(EXTRA_INFO_ROBOT_WORK, "doctor",
                EXTRA_INFO_ROBOT_ABOUT_ME, "about me", EXTRA_INFO_ROBOT_INTEREST, "drink")));
        robotService.addRobot(robot);

        result.put(DATA, RobotInfoView.convertRobot(robot));
        return result;
    }
}
