package com.happy.chat.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {
    private boolean useDefault;

    private String content;

    private String reasonAndModel;

    private String payTips;

    // 规劝提示
    private String systemTips;
}
