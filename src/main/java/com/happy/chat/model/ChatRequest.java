package com.happy.chat.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRequest {
    private String userId;
    private String robotId;
    private String userWord;
    private String robotDefaultResp;
    private List<String> sensitiveWords;
}
