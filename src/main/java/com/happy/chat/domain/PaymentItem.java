package com.happy.chat.domain;

import lombok.Data;

@Data
public class PaymentItem {
    private String userId;
    private String robotId;
    private String sessionId;
    private String extraInfo;
    private String state;
    private long createTime;
    private long updateTime;
}
