package com.happy.chat.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.happy.chat.dao.PaymentDao;
import com.happy.chat.domain.PaymentItem;
import com.happy.chat.domain.UserSubscribeInfo;
import com.happy.chat.enums.PaymentState;
import com.happy.chat.model.CheckoutPayment;
import com.happy.chat.service.PaymentService;
import com.happy.chat.uitls.ObjectMapperUtils;
import com.stripe.model.checkout.Session;

@Lazy
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentDao paymentDao;

    @Override
    public List<String> getUserSubscribeRobotIds(String userId) {
        // 查看 user subscribe 过滤掉是否到期
        List<UserSubscribeInfo> userSubscribeInfos = paymentDao.getUserSubscribeRobotIds(userId);
        return userSubscribeInfos.stream()
                .filter(info -> info.getExpireMills() <= System.currentTimeMillis())
                .map(UserSubscribeInfo::getRobotId)
                .collect(Collectors.toList());

    }

    @Override
    public boolean userHasPayedRobot(String userId, String robotId) {
        // 查看 user subscribe 是否到期
        List<UserSubscribeInfo> userSubscribeInfos = paymentDao.getUserSubscribeRobotIds(userId);
        UserSubscribeInfo userSubscribeInfo = userSubscribeInfos.stream()
                .filter(info -> info.getRobotId().equals(robotId))
                .filter(info -> info.getExpireMills() <= System.currentTimeMillis())
                .findFirst()
                .orElse(null);
        return userSubscribeInfo != null;
    }

    @Override
    public int addPayRequest(CheckoutPayment payment, Session session) {
        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setUserId(payment.getUserId());
        paymentItem.setRobotId(payment.getRobotId());
        paymentItem.setSessionId(payment.getUserId());
        paymentItem.setState(PaymentState.CREATE_SESSION.getState());

        Map<String, String> extraMap = ImmutableMap.of("request", ObjectMapperUtils.toJSON(payment));
        paymentItem.setExtraInfo(ObjectMapperUtils.toJSON(extraMap));
        paymentItem.setCreateTime(System.currentTimeMillis());
        paymentItem.setUpdateTime(System.currentTimeMillis());

        return paymentDao.insertRequest(paymentItem);
    }

    @Override
    public boolean handleUserPaymentSuccess(String sessionId) {
        int update = paymentDao.updateRequestState(sessionId, PaymentState.SUCCESS.getState());
        if (update <= 0) {
            return false;
        }
        // todo 更新下用户的订阅表信息
        return true;
    }
}
