package com.happy.chat.service.impl;

import java.util.ArrayList;
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
        return new ArrayList<>();
    }

    @Override
    public boolean userHasPayedRobot(String userId, String robotId) {
        return false;
    }
}
