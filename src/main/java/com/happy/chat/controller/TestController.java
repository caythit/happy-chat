package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.CHAT_FROM_USER;
import static com.happy.chat.constants.Constant.PERF_TEST_MODULE;
import static com.happy.chat.uitls.CacheKeyProvider.chatFinishPayTipsKey;
import static com.happy.chat.uitls.CacheKeyProvider.chatSensitiveWordKey;
import static com.happy.chat.uitls.CacheKeyProvider.chatSystemTipsKey;
import static com.happy.chat.uitls.CacheKeyProvider.chatUnPayTipsKey;
import static com.happy.chat.uitls.CacheKeyProvider.chatWarnWordKey;
import static com.happy.chat.uitls.CacheKeyProvider.defaultRobotRespChatKey;
import static com.happy.chat.uitls.CacheKeyProvider.startupConfigKey;
import static com.happy.chat.uitls.CacheKeyProvider.userChatgptWarnMaxCountKey;
import static com.happy.chat.uitls.CacheKeyProvider.userEnterChatgptAdvanceModelThresholdKey;
import static com.happy.chat.uitls.CacheKeyProvider.userExitHappyModelExpireMillsKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.happy.chat.domain.FlirtopiaChat;
import com.happy.chat.domain.Robot;
import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.helper.EmailHelper;
import com.happy.chat.model.HappyModelRequest;
import com.happy.chat.model.StartupConfigModel;
import com.happy.chat.service.ChatService;
import com.happy.chat.service.PaymentService;
import com.happy.chat.service.RobotService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.CommonUtils;
import com.happy.chat.uitls.ObjectMapperUtils;
import com.happy.chat.uitls.OkHttpUtils;
import com.happy.chat.uitls.PrometheusUtils;
import com.happy.chat.uitls.RedisUtil;
import com.happy.chat.view.StartupConfigView;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

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

    @Autowired
    private EmailHelper emailHelper;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OkHttpUtils okHttpUtils;

    @Autowired
    private ChatService chatService;


    @RequestMapping("/test")
    public Map<String, Object> test(@RequestParam("userName") String userName) {
        Map<String, Object> result = ApiResult.ofSuccess();



        result.put("data", String.format("hello, %s", userName));

        prometheusUtil.perf(PERF_TEST_MODULE, "test API入口");
        return result;
    }

    @RequestMapping("/insertPrepareData")
    public Map<String, Object> insertPrepareData() {
        Map<String, Object> result = ApiResult.ofSuccess();

        // 敏感词
        redisUtil.delete(chatSensitiveWordKey());
        redisUtil.rightPushAll(chatSensitiveWordKey(), "are LLM", "are robot", "are chat gpt");
        result.put(chatSensitiveWordKey(), redisUtil.range(chatSensitiveWordKey(), 0, -1));

        // 警报
        redisUtil.delete(chatWarnWordKey());
        redisUtil.rightPushAll(chatWarnWordKey(), "chicken and duckling go swimming together");
        result.put(chatWarnWordKey(), redisUtil.range(chatWarnWordKey(), 0, -1));

        // 默认回复
        redisUtil.delete(defaultRobotRespChatKey());
        redisUtil.rightPushAll(defaultRobotRespChatKey(), "It's not funny, you think I'm boring like a robot?");
        result.put(defaultRobotRespChatKey(), redisUtil.range(defaultRobotRespChatKey(), 0, -1));

        // 系统提示 规劝文案
        redisUtil.set(chatSystemTipsKey(), "Don't scare a girl. Do it gently and softly.");
        result.put(chatSystemTipsKey(), redisUtil.get(chatSystemTipsKey()));


        // 促支付文案
        redisUtil.delete(chatUnPayTipsKey());
        redisUtil.rightPushAll(chatUnPayTipsKey(), "Don’t be shy to undress me, only $9.9",
                "Darling i want to serve you better, only $9.9", "$9.9 for R-18 experience.", "I'll show you my true collor, only $9.9",
                "Trying to get to second base with me? Only $9.9", "You want me to give you a tease? Only $9.9");
        result.put(chatUnPayTipsKey(), redisUtil.range(chatUnPayTipsKey(), 0, -1));

        // 支付完成文案
        redisUtil.delete(chatFinishPayTipsKey());
        redisUtil.rightPushAll(chatFinishPayTipsKey(), "I will only serve you at all time.", "I want you so bad.",
                "I am thirsty to have you.", "I love you. Are you rich in pants?", "Let's have a short time but good time",
                "Give me a spanking now!", "I'm so turned on!");
        result.put(chatFinishPayTipsKey(), redisUtil.range(chatFinishPayTipsKey(), 0, -1));


        // 退场机制时间
        redisUtil.set(userExitHappyModelExpireMillsKey(), "300000");
        result.put(userExitHappyModelExpireMillsKey(), redisUtil.get(userExitHappyModelExpireMillsKey()));

        // 聊天请求热情版gpt触发消息条数
        redisUtil.set(userEnterChatgptAdvanceModelThresholdKey(), "10");
        result.put(userEnterChatgptAdvanceModelThresholdKey(), redisUtil.get(userEnterChatgptAdvanceModelThresholdKey()));

        // 聊天普通版gpt警报阈值次数
        redisUtil.set(userChatgptWarnMaxCountKey(), "3");
        result.put(userChatgptWarnMaxCountKey(), redisUtil.get(userChatgptWarnMaxCountKey()));

        StartupConfigModel startupConfig = new StartupConfigModel();
        startupConfig.setAgeOptions(ImmutableList.of("18-25", "25-35", "35+"));
        startupConfig.setIntroduceText("Who would you like to chat with?");
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

    @RequestMapping("/sendMail")
    public Map<String, Object> sendMail(@RequestParam("to") String to, @RequestParam("subject") String subject) {
        ErrorEnum errorEnum = emailHelper.sendCode(to, subject, false, "test");
        if (errorEnum == ErrorEnum.SUCCESS) {
            return ApiResult.ofSuccess();
        }
        return ApiResult.ofFail(errorEnum);
    }

    @RequestMapping("/handleUserPaymentSuccess")
    public Map<String, Object> handleUserPaymentSuccess(@RequestParam("sessionId") String sessionId) {
        Map<String, Object> result = ApiResult.ofSuccess();
        result.put("result", paymentService.handleUserPaymentSuccess(sessionId));
        return result;
    }

    @RequestMapping("/userHasPayedRobot")
    public Map<String, Object> userHasPayedRobot(@RequestParam("userId") String userId,
                                                 @RequestParam("robotId") String robotId) {
        Map<String, Object> result = ApiResult.ofSuccess();
        result.put("result", paymentService.userHasPayedRobot(userId, robotId));
        return result;
    }

    @RequestMapping("/requestHappyModel")
    public Map<String, Object> requestHappyModel(@RequestParam("userId") String userId,
                                                 @RequestParam("robotId") String robotId,
                                                 @RequestParam("currentUserInput") String currentUserInput) {
        Map<String, Object> result = ApiResult.ofSuccess();

        List<FlirtopiaChat> historyChats = chatService.getUserRobotHistoryChats(userId, robotId).stream()
                .sorted(Comparator.comparing(FlirtopiaChat::getCreateTime)).collect(Collectors.toList());

        HappyModelRequest happyModelRequest = HappyModelRequest.builder()
                .temperature(0.1)
                .maxNewToken(100)
                .historyMaxLen(1000)
                .topP(0.85)
                .userId(robotId)
                .current(HappyModelRequest.Current.builder()
                        .u(currentUserInput)
                        .build())
                .build();

        List<HappyModelRequest.History> histories = new ArrayList<>();

        // 转换成格式
        historyChats.forEach(historyChat -> {
            if (CHAT_FROM_USER.equals(historyChat.getMessageFrom())) {
                histories.add(HappyModelRequest.History.builder()
                        .u(historyChat.getContent())
                        .build());
            } else {
                histories.add(HappyModelRequest.History.builder()
                        .b(historyChat.getContent())
                        .build());
            }
        });
        happyModelRequest.setHistory(histories);
        log.info("request {}", ObjectMapperUtils.toJSON(happyModelRequest));
        try {
            Response response = okHttpUtils.postJson("http://52.14.18.78:5000/chat", ObjectMapperUtils.toJSON(happyModelRequest));
            String json;
            if (response != null && response.body() != null) {
                json = response.body().string();
                log.info("response json {}", json);
                Map<String, String> jsonMap = ObjectMapperUtils.fromJSON(json, Map.class, String.class, String.class);
                result.put("response", ObjectMapperUtils.toJSON(jsonMap));
            }
        } catch (Exception e) {
            log.error("requestHappyModel exception", e);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(CommonUtils.encryptPwd("stripe.apiKey", "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"));
        System.out.println(CommonUtils.encryptPwd("stripe.webhookSecret", "whsec_Qcu5aOukJ42lDzUfGe7AE4snFLGycmYO"));
        System.out.println(CommonUtils.encryptPwd("stripe.apiKey", "sk_test_51NPOMIBegHkiPVmEtyhXq3hPFQrXrx9oTVeobQ07bG7PKCloN8uAk4YVq9kPBbb46SgeMocpKSD9TQwuhGcJv1St000ngXvqSb"));
        System.out.println(CommonUtils.encryptPwd("stripe.webhookSecret", "whsec_tMGmi5ui6dpSwO6oNaSoqNIUxaMP7xu8"));
    }
}