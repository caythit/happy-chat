package com.happy.chat.service.impl;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.happy.chat.service.OpenAIService;
import com.happy.chat.uitls.ObjectMapperUtils;
import com.happy.chat.uitls.PrometheusUtils;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Component
@Slf4j
public class OpenAIServiceImpl implements OpenAIService {

    @Autowired
    private PrometheusUtils prometheusUtil;

    /**
     * /v1/chat/completions: gpt-4, gpt-4-0314, gpt-4-32k, gpt-4-32k-0314, gpt-3.5-turbo, gpt-3.5-turbo-0301
     * /v1/completions:    text-davinci-003, text-davinci-002, text-curie-001, text-babbage-001, text-ada-001
     * <p>
     * 参考
     * https://stackoverflow.com/questions/76192496/openai-v1-completions-vs-v1-chat-completions-end-points
     *
     * @param messages
     * @return
     */
    @Override
    public ChatMessage requestChatCompletion(List<String> apiTokens, List<ChatMessage> messages) {
        for (String apiToken : apiTokens) {
            OpenAiService service = new OpenAiService(apiToken);

            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .n(1)
//                .temperature()
                    .maxTokens(100)
                    .logitBias(new HashMap<>())
                    .build();
            ChatCompletionResult chatCompletionResult = service.createChatCompletion(chatCompletionRequest);
            if (chatCompletionResult == null || CollectionUtils.isEmpty(chatCompletionResult.getChoices())) {
                log.error("gpt return empty {}", ObjectMapperUtils.toJSON(chatCompletionRequest));
                prometheusUtil.perf("chat_open_ai_return_empty");
                continue;
            }
            return chatCompletionResult.getChoices().get(0).getMessage();
        }
        return null;
    }

}
