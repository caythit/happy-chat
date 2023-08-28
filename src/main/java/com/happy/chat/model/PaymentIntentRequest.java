package com.happy.chat.model;

import lombok.Data;

@Data
public class PaymentIntentRequest {
    private String userId;
    private String robotId;
    private String successUrl;
    private String currency;
    private long amount;
    private long quantity;
}
