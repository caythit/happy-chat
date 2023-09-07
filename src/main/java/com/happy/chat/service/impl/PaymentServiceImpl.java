package com.happy.chat.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;
import com.happy.chat.dao.PaymentDao;
import com.happy.chat.domain.PaymentItem;
import com.happy.chat.domain.UserSubscribeInfo;
import com.happy.chat.enums.PaymentState;
import com.happy.chat.model.CheckoutRequest;
import com.happy.chat.service.PaymentService;
import com.happy.chat.uitls.ObjectMapperUtils;
import com.happy.chat.uitls.PrometheusUtils;
import com.stripe.model.checkout.Session;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Lazy
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private PrometheusUtils prometheusUtil;

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
    public int addPayRequest(CheckoutRequest payment, Session session) {
        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setUserId(payment.getUserId());
        paymentItem.setRobotId(payment.getRobotId());
        paymentItem.setSessionId(session.getId());
        paymentItem.setState(PaymentState.INIT.getState());

        Map<String, String> extraMap = ImmutableMap.of("request", ObjectMapperUtils.toJSON(payment));
        paymentItem.setExtraInfo(ObjectMapperUtils.toJSON(extraMap));
        paymentItem.setCreateTime(System.currentTimeMillis());
        paymentItem.setUpdateTime(System.currentTimeMillis());

        return paymentDao.insertRequest(paymentItem);
    }

    @Override
    public int addPayRequest(String userId, String robotId, String id, String extraInfo) {
        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setUserId(userId);
        paymentItem.setRobotId(robotId);
        paymentItem.setSessionId(id);
        paymentItem.setState(PaymentState.INIT.getState());
        paymentItem.setExtraInfo(extraInfo);

        paymentItem.setCreateTime(System.currentTimeMillis());
        paymentItem.setUpdateTime(System.currentTimeMillis());

        return paymentDao.insertRequest(paymentItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public boolean handleUserPaymentSuccess(String sessionId) {
        // 先拿到sessionId对应的用户和robot信息
        PaymentItem paymentItem = paymentDao.getPaymentRequest(sessionId);
        if (paymentItem == null) {
            log.error("can not find payment request {}", sessionId);
            return false;
        }
        long expire = System.currentTimeMillis() + Duration.ofDays(30).toMillis();
        // 写入/更新下用户的订阅表信息
        int update = paymentDao.updateUserSubscribeRobot(paymentItem.getUserId(), paymentItem.getRobotId(), expire);
        if (update <= 0) {
            log.error("updateUserSubscribeRobot failed {} {} {}", paymentItem.getUserId(), paymentItem.getRobotId(), sessionId);
            return false;
        }
        // 更新付款请求表状态
        update = paymentDao.updateRequestState(sessionId, PaymentState.SUCCESS.getState());
        if (update <= 0) {
            log.error("updateRequestState failed {}", sessionId);
            throw new RuntimeException("更新状态失败，回滚!");
        }
        return true;
    }
}
