package com.happy.chat.service.impl;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.happy.chat.service.PaymentService;
import com.happy.chat.service.UserService;

@Lazy
@Service
public class PaymentServiceImpl implements PaymentService {


    @Override
    public List<String> getUserSubscribeRobotIds(String userId) {
        return null;
    }

    @Override
    public boolean userHasPayedRobot(String userId, String robotId) {
        return false;
    }
}
