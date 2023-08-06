package com.happy.chat.service.impl;

import static com.happy.chat.uitls.PrometheusUtils.perf;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.happy.chat.service.OpenAIService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;

@Lazy
@Component
@Slf4j
public class OpenAIServiceImpl implements OpenAIService {

    private final String prometheusName = "openAI";
    private final String prometheusHelp = "openAI";

    @Value("${com.flirtopia.openai.token}")
    private String token;

    @Autowired
    private CollectorRegistry openaiRegistry;

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
    public ChatMessage requestChatCompletion(List<ChatMessage> messages) {
        OpenAiService service = new OpenAiService(token);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();
        ChatCompletionResult chatCompletionResult = service.createChatCompletion(chatCompletionRequest);
        if (chatCompletionResult == null || CollectionUtils.isEmpty(chatCompletionResult.getChoices())) {
            log.error("gpt return empty");
            perf(openaiRegistry, prometheusName, prometheusHelp, "chat_gpt_return_empty");
            return null;
        }
        return chatCompletionResult.getChoices().get(0).getMessage();
    }
}
