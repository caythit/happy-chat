package com.happy.chat.service;

import java.util.List;

import com.happy.chat.model.CheckoutRequest;
import com.stripe.model.checkout.Session;

public interface PaymentService {
    List<String> getUserSubscribeRobotIds(String userId);

    boolean userHasPayedRobot(String userId, String robotId);

    int addPayRequest(CheckoutRequest payment, Session session);

    boolean handleUserPaymentSuccess(String sessionId);

    int addPayRequest(String userId, String robotId, String id);
}
