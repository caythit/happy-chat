package com.happy.chat.service;

import java.util.List;
import java.util.function.Function;

import com.happy.chat.domain.ChatMessage;
import com.happy.chat.domain.IceBreakWord;
import com.happy.chat.model.ChatResponse;

public interface ChatService {
    List<IceBreakWord> getIceBreakWordsByRobot(String robotId);

    List<ChatMessage> getUserChatMessage(String userId);

    int insert(ChatMessage chatMessage);

    int batchInsert(List<ChatMessage> chatMessages);

    ChatResponse requestChat(String userId, String robotId, String content, Function<String, List<ChatMessage>> userHistoryChatFunc);

    List<ChatMessage> getUserRobotChatMessage(String userId, String robotId);
}
