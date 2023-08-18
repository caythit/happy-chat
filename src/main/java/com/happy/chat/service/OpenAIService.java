package com.happy.chat.service;

import java.util.List;

import com.theokanning.openai.completion.chat.ChatMessage;

public interface OpenAIService {

    ChatMessage requestChatCompletion(String apiToken, List<ChatMessage> messages);

}
