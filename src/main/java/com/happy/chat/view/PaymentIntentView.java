package com.happy.chat.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentIntentView {
    private String requestId;
    private String priceId;
    private String currency;
    private String unitAmount;
}
