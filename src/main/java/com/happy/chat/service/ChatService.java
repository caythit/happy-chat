package com.happy.chat.service;

import java.util.List;

import com.happy.chat.domain.FlirtopiaChat;
import com.happy.chat.domain.IceBreakWord;
import com.happy.chat.model.ChatRequest;
import com.happy.chat.model.ChatResponse;

public interface ChatService {
    List<IceBreakWord> getIceBreakWordsByRobot(String robotId);

    List<FlirtopiaChat> getUserHistoryChats(String userId);

    int insert(FlirtopiaChat flirtopiaChat);

    int batchInsert(List<FlirtopiaChat> flirtopiaChats);

    ChatResponse requestChat(ChatRequest chatRequest);

    List<FlirtopiaChat> getUserRobotHistoryChats(String userId, String robotId);
}
