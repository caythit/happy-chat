package com.happy.chat.service.impl;

import static com.happy.chat.uitls.CacheKeyProvider.chatSensitiveWordKey;
import static com.happy.chat.uitls.CacheKeyProvider.defaultRobotRespChatKey;
import static com.happy.chat.uitls.PrometheusUtils.perf;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.happy.chat.dao.ChatMessageDao;
import com.happy.chat.domain.ChatMessage;
import com.happy.chat.domain.IceBreakWord;
import com.happy.chat.model.ChatResponse;
import com.happy.chat.service.ChatService;
import com.happy.chat.uitls.RedisUtil;

import io.prometheus.client.CollectorRegistry;

@Lazy
@Service
public class ChatServiceImpl implements ChatService {

    private final String prometheusName = "chat";
    private final String prometheusHelp = "chat_operation";

    @Autowired
    private CollectorRegistry chatRegistry;

    @Autowired
    private ChatMessageDao chatMessageDao;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<IceBreakWord> getIceBreakWordsByRobot(String robotId) {
        return chatMessageDao.getRobotIceBreakWords(robotId);
    }

    @Override
    public List<ChatMessage> getUserChatMessage(String userId) {
        return chatMessageDao.getUserChatList(userId);
    }

    @Override
    public List<ChatMessage> getUserRobotChatMessage(String userId, String robotId) {
        return chatMessageDao.getUserRobotChats(userId, robotId);
    }

    @Override
    public int insert(ChatMessage chatMessage) {
        return chatMessageDao.insertChat(chatMessage);
    }

    @Override
    public ChatResponse requestChat(String userId, String robotId, String content,
                                    Function<String, List<ChatMessage>> userHistoryChatFunc) {

        boolean hasSensitiveWord = hasSensitiveWord(content);
        // 敏感词 直接返回
        if (hasSensitiveWord) {
            // return default resp
            List<String> defaultResps = redisUtil.range(defaultRobotRespChatKey(), 0, -1);
            if (CollectionUtils.isEmpty(defaultResps)) {
                perf(chatRegistry, prometheusName, prometheusHelp, "robot_default_resp_chat_empty");
                // todo 默认的
                return new ChatResponse("xxxx", "");
            } else {
                return new ChatResponse(defaultResps.get(RandomUtils.nextInt(0, defaultResps.size())), "");
            }
        }
        // 请求reco 根据用户聊天次数请求不同的服务
        // 返回警报 警报次数 快乐模型带出付费卡片

        return null;
    }

    private boolean hasSensitiveWord(String content) {
        List<String> sensitiveWords = redisUtil.range(chatSensitiveWordKey(), 0, -1);
        if (CollectionUtils.isEmpty(sensitiveWords)) {
            perf(chatRegistry, prometheusName, prometheusHelp, "chat_sensitive_word_empty");
            return false;
        }
        perf(chatRegistry, prometheusName, prometheusHelp, "hit_chat_sensitive_word");
        return sensitiveWords.stream()
                .anyMatch(content::contains);
    }

    @Override
    public int batchInsert(List<ChatMessage> chatMessages) {
        return chatMessageDao.batchInsertChat(chatMessages);
    }
}
