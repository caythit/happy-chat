package com.happy.chat.domain;

import lombok.Data;

@Data
public class UserSubscribeInfo {
    private String userId;
    private String robotId;
    private long expireMills;
}
