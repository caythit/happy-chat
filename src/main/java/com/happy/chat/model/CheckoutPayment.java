package com.happy.chat.model;

import lombok.Data;

@Data
public class CheckoutPayment {
    private String userId;
    private String robotId;
    // our success and cancel url stripe will redirect to this links
    private String successUrl;
    private String cancelUrl;
    //  Prices define the unit cost, currency, and (optional) billing cycle for both recurring and one-time purchases of products.
    private String priceId;
    private long quantity;
}
