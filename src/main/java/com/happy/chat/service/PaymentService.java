package com.happy.chat.service;

import java.util.List;

public interface PaymentService {
    List<String> getUserSubscribeRobotIds(String userId);

    boolean userHasPayedRobot(String userId, String robotId);
}
