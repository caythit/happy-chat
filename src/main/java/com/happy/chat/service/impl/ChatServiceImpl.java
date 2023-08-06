package com.happy.chat.service.impl;

import static com.happy.chat.constants.Constant.CHAT_FROM_USER;
import static com.happy.chat.uitls.CacheKeyProvider.chatSensitiveWordKey;
import static com.happy.chat.uitls.CacheKeyProvider.chatUnPayWordKey;
import static com.happy.chat.uitls.CacheKeyProvider.chatWarnWordKey;
import static com.happy.chat.uitls.CacheKeyProvider.defaultRobotRespChatKey;
import static com.happy.chat.uitls.CacheKeyProvider.userChatgptWarnKey;
import static com.happy.chat.uitls.CacheKeyProvider.userChatgptWarnMaxCountKey;
import static com.happy.chat.uitls.CacheKeyProvider.userEnterChatgptAdvanceModelThresholdKey;
import static com.happy.chat.uitls.CacheKeyProvider.userEnterHappyModelLatestTimeKey;
import static com.happy.chat.uitls.CacheKeyProvider.userExitHappyModelExpireMillsKey;
import static com.happy.chat.uitls.CacheKeyProvider.userGptPromptKey;
import static com.happy.chat.uitls.PrometheusUtils.perf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.happy.chat.dao.FlirtopiaChatDao;
import com.happy.chat.domain.FlirtopiaChat;
import com.happy.chat.domain.IceBreakWord;
import com.happy.chat.model.ChatResponse;
import com.happy.chat.service.ChatService;
import com.happy.chat.service.OpenAIService;
import com.happy.chat.service.PaymentService;
import com.happy.chat.uitls.OkHttpUtils;
import com.happy.chat.uitls.RedisUtil;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;

@Lazy
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final String prometheusName = "chat";
    private final String prometheusHelp = "chat_operation";

    private final String defaultHappyModelExitExpireTime = "300000";
    private final String defaultEnterAdvanceModelHistoryChatSize = "10";
    private final String defaultUserChatWarnMaxCount = "3";

    private final String normalVersionGpt = "normal";

    private final String advancedVersionGpt = "advanced";

    @Autowired
    private CollectorRegistry chatRegistry;

    @Autowired
    private FlirtopiaChatDao flirtopiaChatDao;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private OkHttpUtils okHttpUtils;

    @Override
    public List<IceBreakWord> getIceBreakWordsByRobot(String robotId) {
        return flirtopiaChatDao.getRobotIceBreakWords(robotId);
    }

    @Override
    public List<FlirtopiaChat> getUserHistoryChats(String userId) {
        return flirtopiaChatDao.getUserChatList(userId);
    }

    @Override
    public List<FlirtopiaChat> getUserRobotHistoryChats(String userId, String robotId) {
        return flirtopiaChatDao.getUserRobotChats(userId, robotId);
    }

    @Override
    public int insert(FlirtopiaChat flirtopiaChat) {
        return flirtopiaChatDao.insertChat(flirtopiaChat);
    }

    /**
     * 1、用户输入内容有敏感词，直接返回默认回复（取缓存数据，若没有取到则返回I have no idea）
     * <p>
     * 2、用户输入内容无敏感词
     * 拿出用户和AI的历史聊天记录，按时间前后顺序排序，用作请求gpt和快乐模型的参数输入。
     * <p>
     * 前置说明，快乐模型退场机制检测：用户进入快乐模型，一段时间未聊天后，再次正常聊天，不再使用快乐模型，避免回复崩塌。
     * 逻辑上就是 check 进没进过快乐模型 或者 "取缓存里记录的：调用的快乐模型的上一次时间" 和 "当前时间"做比较，未超过 "缓存配置的阈值，比如5分钟"
     * <p>
     * 2-1）已付费用户，首先进入快乐模型退场机制检测。若退场，则降级调用gpt热情版，若没有退场，则调用快乐模型继续回复。
     * 对回复内容做检测：空或者包含敏感词返回默认回复，见上
     * <p>
     * 同时，当不是默认回复且内容来自快乐模型，要更新下这次调用的快乐模型的当前时间，用于退场机制检测。
     * <p>
     * 2-2）未付费用户，也要做下快乐模型的退场检测（未进入认为是退场，进入过且超过时间认为是退场）。
     * 2-2-1）若已处在快乐模型状态下即未退场（虽然内容是高斯模糊），那么就直接调用快乐模型回复，对回复内容做检测，空或者包含敏感词返回默认回复，见上
     * 否则返回内容的同时要加一条付费提示
     * <p>
     * 2-2-2）若不在快乐模型状态下即退场，那么去缓存里拿对应配置的历史聊天轮次数阈值（没取到使用硬编码写死5轮）
     * 比较历史聊天轮次是否超过配置的阈值，若超过则需要调用热情版gpt，否则需要调用普通版gpt
     * 调用gpt前需要组装一些参数，包括但不限于：robot的prompt信息（人设）、警报关键词、组装当前聊天和历史聊天等
     * 同样也要对回复内容做检测，空或者包含敏感词返回默认回复，见上。否则去看下警报关键词：
     * <p>
     * 出现了警报关键词的情况，逻辑如下：
     * 2-2-2-1）普通版：去缓存里拿出配置的警报次数阈值（没取到使用硬编码写死3次），比较用户和该ai的触发警报次数是否超过配置的阈值
     * 小于等于阈值，则返回客户端回复内容外还要加一条系统提示（也是从缓存里取）
     * 大于阈值，调用快乐模型。对快乐模型的回复内容同样做检测，空或者敏感词，返回默认回复，否则返回客户端回复内容外还要告诉客户端该内容付费要加上高斯模糊
     * 2-2-2-2）热情版：调用快乐模型，对快乐模型的回复内容同样做检测，空或者敏感词，返回默认回复，否则返回客户端回复内容外还要告诉客户端该内容付费要加上高斯模糊
     *
     * @param userId
     * @param robotId
     * @param content
     * @return
     */
    @Override
    public ChatResponse requestChat(String userId, String robotId, String content) {

        boolean hasSensitiveWord = hasSensitiveWord(content);
        // 敏感词 直接返回
        if (hasSensitiveWord) {
            log.warn("user request content contains sensitive {} {} {}", userId, robotId, content);
            perf(chatRegistry, prometheusName, prometheusHelp, "chat_user_request_content_contains_sensitive", userId, robotId);
            return getRobotDefaultResp();
        }

        //  按时间升序排序
        List<FlirtopiaChat> userHistoryMessages = getUserHistoryChats(userId).stream()
                .sorted(Comparator.comparing(FlirtopiaChat::getCreateTime)).collect(Collectors.toList());
        // 是否付费 若付费，直接调用快乐模型，调用前检查是否超过快乐模型过期时间，若已经过期，则降级调用热情版gpt
        boolean hasPay = paymentService.userHasPayedRobot(userId, robotId);
        if (hasPay) {
            return getRobotAlreadyPaidResp(userId, robotId, content, userHistoryMessages);
        }
        // 未付费请求
        return getRobotUnPaidResp(userId, robotId, content, userHistoryMessages);
    }

    /**
     * 未付费的ai回复
     *
     * @param userId
     * @param robotId
     * @param content
     * @param userHistoryMessages
     * @return
     */
    private ChatResponse getRobotUnPaidResp(String userId, String robotId, String content, List<FlirtopiaChat> userHistoryMessages) {
        // 做下快乐模型退场检测
        boolean timeForExit = checkTimeForExitHappyModel(userId, robotId);
        // 若没退场 使用快乐模型回复
        if (!timeForExit) {
            return getRobotUnPaidRespFromHappyModel(userId, robotId, content, userHistoryMessages);
        }

        // 退场，轮次够，使用热情版gpt
        if (isEnterChatgptAdvancedModel(userHistoryMessages)) {
            return getRobotUnPaidRespFromAdvancedGpt(userId, robotId, content, userHistoryMessages);
        }
        // 退场，轮次不够，使用热情版gpt
        return getRobotUnPaidRespFromNormalGpt(userId, robotId, content, userHistoryMessages);
    }

    /**
     * 未付费 快乐模型回复
     *
     * @param userId
     * @param robotId
     * @param content
     * @param userHistoryMessages
     * @return
     */
    private ChatResponse getRobotUnPaidRespFromHappyModel(String userId, String robotId,
                                                          String content, List<FlirtopiaChat> userHistoryMessages) {
        String aiRespContent = requestHappyModel(content, userHistoryMessages);
        ChatResponse chatResponse = buildChatResponse(userId, robotId, aiRespContent);

        // 来自快乐模型的回复 要更新下时间
        if (!chatResponse.isUseDefault()) {
            updateHappyModelLatestTime(userId, robotId);
            // todo 同时返回付费提示 客户端高斯模糊
            chatResponse.setPayTips(redisUtil.getOrDefault(chatUnPayWordKey(), "xxxx"));
        }
        return chatResponse;
    }

    /**
     * 未付费 热情版gpt回复
     *
     * @param userId
     * @param robotId
     * @param content
     * @param userHistoryMessages
     * @return
     */
    private ChatResponse getRobotUnPaidRespFromAdvancedGpt(String userId, String robotId,
                                                           String content, List<FlirtopiaChat> userHistoryMessages) {
        String respContent = requestChatgpt(robotId, advancedVersionGpt, content, userHistoryMessages);
        boolean fromHappyModel = false;

        // 触发警报，请求快乐模型
        if (chatgptRespHasWarn(respContent)) {
            respContent = requestHappyModel(content, userHistoryMessages);
            fromHappyModel = true;
        }

        ChatResponse chatResponse = buildChatResponse(userId, robotId, respContent);
        // 来自快乐模型 要付费，记下时间
        if (!chatResponse.isUseDefault() && fromHappyModel) {
            updateHappyModelLatestTime(userId, robotId);
            // todo
            chatResponse.setPayTips(redisUtil.getOrDefault(chatUnPayWordKey(), "xxxx"));
        }
        return chatResponse;
    }

    /**
     * 未付费 普通版gpt回复
     *
     * @param userId
     * @param robotId
     * @param content
     * @param userHistoryMessages
     * @return
     */
    private ChatResponse getRobotUnPaidRespFromNormalGpt(String userId, String robotId,
                                                         String content, List<FlirtopiaChat> userHistoryMessages) {
        // 退场，使用普通版gpt
        String respContent = requestChatgpt(robotId, normalVersionGpt, content, userHistoryMessages);
        boolean fromHappyModel = false;
        boolean hasWarn = false;
        // 触发警报
        if (chatgptRespHasWarn(respContent)) {
            //  超过3次，请求快乐模型
            boolean overWarnCount = overGptWarnCount(userId, robotId);
            if (overWarnCount) {
                respContent = requestHappyModel(content, userHistoryMessages);
                fromHappyModel = true;
            } else {
                // warn次数+1
                addGptWarnCount(userId, robotId);
                hasWarn = true;
            }
        }

        ChatResponse chatResponse = buildChatResponse(userId, robotId, respContent);
        // 来自快乐模型 要付费，记下时间
        if (!chatResponse.isUseDefault()) {
            if (fromHappyModel) {
                updateHappyModelLatestTime(userId, robotId);
                // todo
                chatResponse.setPayTips(redisUtil.getOrDefault(chatUnPayWordKey(), "xxxx"));
            }
            if (hasWarn) {
                // todo
                chatResponse.setWarnTips(redisUtil.getOrDefault(chatWarnWordKey(), "xxxx"));
            }
        }
        return chatResponse;
    }

    /**
     * 付费后的ai回复
     *
     * @param userId
     * @param robotId
     * @param userReqContent
     * @param userHistoryMessages
     * @return
     */
    private ChatResponse getRobotAlreadyPaidResp(String userId, String robotId, String userReqContent,
                                                 List<FlirtopiaChat> userHistoryMessages) {
        // 快乐模型是否退场机制
        boolean timeForExit = checkTimeForExitHappyModel(userId, robotId);
        String aiRespContent;
        boolean fromHappyModel = false;
        if (timeForExit) {
            aiRespContent = requestChatgpt(robotId, advancedVersionGpt, userReqContent, userHistoryMessages);
        } else {
            aiRespContent = requestHappyModel(userReqContent, userHistoryMessages);
            fromHappyModel = true;
        }

        ChatResponse chatResponse = buildChatResponse(userId, robotId, aiRespContent);
        // 来自快乐模型的回复 要更新下时间
        if (!chatResponse.isUseDefault() && fromHappyModel) {
            updateHappyModelLatestTime(userId, robotId);
        }
        return chatResponse;
    }

    private ChatResponse buildChatResponse(String userId, String robotId, String content) {
        // 为空
        if (StringUtils.isEmpty(content)) {
            log.error("getRobotAlreadyPaidResp empty {} {} {}", userId, robotId, content);
            perf(chatRegistry, prometheusName, prometheusHelp, "chat_ai_resp_empty", userId, robotId);
            return getRobotDefaultResp();
        }
        // 敏感词
        if (hasSensitiveWord(content)) {
            log.error("getRobotAlreadyPaidResp hasSensitiveWord {} {} {}", userId, robotId, content);
            perf(chatRegistry, prometheusName, prometheusHelp, "chat_ai_resp_contains_sensitive", userId, robotId);
            return getRobotDefaultResp();
        }

        return ChatResponse.builder()
                .content(content)
                .build();
    }

    // 默认兜底回复
    private ChatResponse getRobotDefaultResp() {
        List<String> defaultResps = redisUtil.range(defaultRobotRespChatKey(), 0, -1);
        if (CollectionUtils.isEmpty(defaultResps)) {
            log.error("robot default resp empty");
            perf(chatRegistry, prometheusName, prometheusHelp, "robot_default_resp_chat_empty");
            // todo 默认的
            return ChatResponse.builder()
                    .content("I have no idea about it")
                    .useDefault(true)
                    .build();
        }
        return ChatResponse.builder()
                .content(defaultResps.get(RandomUtils.nextInt(0, defaultResps.size())))
                .useDefault(true)
                .build();
    }

    /**
     * 是否进入chatgpt 热情版
     *
     * @param userHistoryMessages
     * @return
     */
    private boolean isEnterChatgptAdvancedModel(List<FlirtopiaChat> userHistoryMessages) {
        String val = redisUtil.getOrDefault(userEnterChatgptAdvanceModelThresholdKey(), defaultEnterAdvanceModelHistoryChatSize);
        return userHistoryMessages.size() >= Integer.parseInt(val);
    }

    // todo
    private boolean chatgptRespHasWarn(String content) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }
        return content.contains("");
    }

    private void addGptWarnCount(String userId, String robotId) {
        redisUtil.increment(userChatgptWarnKey(userId, robotId), 1L);
    }

    private boolean overGptWarnCount(String userId, String robotId) {
        String userChatWarnCount = redisUtil.get(userChatgptWarnKey(userId, robotId));
        if (StringUtils.isEmpty(userChatWarnCount)) {
            return false;
        }
        String userChatWarnThreshold = redisUtil.getOrDefault(userChatgptWarnMaxCountKey(), defaultUserChatWarnMaxCount);
        return Integer.parseInt(userChatWarnCount) >= Integer.parseInt(userChatWarnThreshold);
    }


    private void updateHappyModelLatestTime(String userId, String robotId) {
        redisUtil.set(userEnterHappyModelLatestTimeKey(userId, robotId), System.currentTimeMillis() + "");
    }

    // 快乐模型退场检查
    private boolean checkTimeForExitHappyModel(String userId, String robotId) {
        String latestTimeStr = redisUtil.get(userEnterHappyModelLatestTimeKey(userId, robotId));
        // 为空 认为是退场 说明历史上根本没有进入过快乐模型
        if (StringUtils.isEmpty(latestTimeStr)) {
            return true;
        }
        String exitExpireTimeStr = redisUtil.getOrDefault(userExitHappyModelExpireMillsKey(), defaultHappyModelExitExpireTime);
        // 超过五分钟没聊 就退出
        if (System.currentTimeMillis() - Long.parseLong(latestTimeStr) >= Long.parseLong(exitExpireTimeStr)) {
            return true;
        }
        return false;
    }

    // todo
    private String requestHappyModel(String currentUserInput, List<FlirtopiaChat> historyChats) {
//        Response response = okHttpUtils.postJson();
        return null;
    }

    /**
     * 请求openAI
     *
     * @param robotId
     * @param version
     * @param currentUserInput
     * @param historyChats
     * @return
     */
    private String requestChatgpt(String robotId, String version, String currentUserInput, List<FlirtopiaChat> historyChats) {
        // 从缓存里取出robot对应的prompt，分成热情版/普通版。即role=system
        String prompt = redisUtil.get(userGptPromptKey(robotId, version));
        if (StringUtils.isEmpty(prompt)) {
            perf(chatRegistry, prometheusName, prometheusHelp, "chat_robot_prompt_empty", robotId, version);
            return null;
        }

        List<ChatMessage> messages = new ArrayList<>();
        // role=system,系统设定，即prompt; role=user,用户输入；role=assistant,gpt输入
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), prompt);
        messages.add(systemMessage);
        // 转换成格式
        historyChats.forEach(historyChat -> {
            if (CHAT_FROM_USER.equals(historyChat.getMessageFrom())) {
                messages.add(new ChatMessage(ChatMessageRole.USER.value(), historyChat.getContent()));
            } else {
                messages.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), historyChat.getContent()));
            }
        });
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), currentUserInput));
        ChatMessage response = openAIService.requestChatCompletion(messages);
        return response == null ? null : response.getContent();
    }

    private boolean hasSensitiveWord(String content) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }
        List<String> sensitiveWords = redisUtil.range(chatSensitiveWordKey(), 0, -1);
        if (CollectionUtils.isEmpty(sensitiveWords)) {
            perf(chatRegistry, prometheusName, prometheusHelp, "chat_sensitive_word_empty");
            return false;
        }
        return sensitiveWords.stream()
                .anyMatch(content::contains);
    }

    @Override
    public int batchInsert(List<FlirtopiaChat> flirtopiaChats) {
        return flirtopiaChatDao.batchInsertChat(flirtopiaChats);
    }
}
