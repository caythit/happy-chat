package com.happy.chat.model;

import lombok.Data;

@Data
public class ChatResponse {
    private String content;

    private String payTips;

    public ChatResponse(String content, String payTips) {
        this.content = content;
        this.payTips = payTips;
    }
}
