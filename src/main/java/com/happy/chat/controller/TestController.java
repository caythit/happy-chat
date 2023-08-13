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
import static com.happy.chat.uitls.CacheKeyProvider.robotGptPromptKey;
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
        result.put("prompt", redisUtil.get(robotGptPromptKey("rb_XXXX5", "advanced")));

        log.info("test log...");
        log.warn("test log...");
        log.error("test log...");
        return result;
    }

    @RequestMapping("/insertStartupConfig")
    public Map<String, Object> insertStartupConfig() {
        Map<String, Object> result = ApiResult.ofSuccess();
        StartupConfigView startupConfigView = new StartupConfigView();
        startupConfigView.setLogoUrl("https://www.freepnglogos.com/uploads/spotify-logo-png/spotify-icon-marilyn-scott-0.png");
        startupConfigView.setAgeOptions(ImmutableList.of("18-25", "25-35", "35+"));
        startupConfigView.setIntroduceText("Hello!\n who would you like\n to chat with?");
        startupConfigView.setWelcomeText("How are you \ntoday?");
        startupConfigView.setPymlRobots(ImmutableList.<StartupConfigView.RobotStartupView>builder()
                .add(new StartupConfigView.RobotStartupView("rb_vMFrG5v0PrIZwPMfSjL3d", "https://c-ssl.duitang.com/uploads/blog/202207/29/20220729075441_ecced.jpeg", "chen"))
                .add(new StartupConfigView.RobotStartupView("rb_XXXX1", "https://c-ssl.duitang.com/uploads/blog/202207/29/20220729075441_ecced.jpeg", "li"))
                .add(new StartupConfigView.RobotStartupView("rb_XXXX2", "https://c-ssl.duitang.com/uploads/blog/202207/29/20220729075441_ecced.jpeg", "xmxmcxmzxmcmzcmzxcmzxmcmxzc"))
                .add(new StartupConfigView.RobotStartupView("rb_XXXX3", "https://c-ssl.duitang.com/uploads/blog/202207/29/20220729075441_ecced.jpeg", "wang"))
                .add(new StartupConfigView.RobotStartupView("rb_XXXX4", "https://c-ssl.duitang.com/uploads/blog/202207/29/20220729075441_ecced.jpeg", "sun"))
                .add(new StartupConfigView.RobotStartupView("rb_XXXX5", "https://c-ssl.duitang.com/uploads/blog/202207/29/20220729075441_ecced.jpeg", "lisdk"))
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
