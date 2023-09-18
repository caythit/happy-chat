package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.DATA;
import static com.happy.chat.constants.Constant.PERF_ERROR_MODULE;
import static com.happy.chat.constants.Constant.PERF_FEED_MODULE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.happy.chat.domain.Robot;
import com.happy.chat.domain.User;
import com.happy.chat.model.UserGetRequest;
import com.happy.chat.service.PaymentService;
import com.happy.chat.service.RobotService;
import com.happy.chat.service.UserService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.PrometheusUtils;
import com.happy.chat.view.FeedView;
import com.happy.chat.view.RobotInfoView;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Component
@Slf4j
public class FeedApiHelper {
    @Autowired
    private PrometheusUtils prometheusUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private RobotService robotService;

    @Autowired
    private PaymentService paymentService;

    public Map<String, Object> foryou(String userId, String dummyUid, int size) {
        Map<String, Object> result = ApiResult.ofSuccess();

        FeedView feedView = new FeedView();
        User user;
        List<String> userSubscribeRobotIds = new ArrayList<>();
        // 登陆了,拿名字
        if (StringUtils.isNotEmpty(userId)) {
            user = userService.getUser(UserGetRequest.builder().userId(userId).build());
            feedView.setUserName(user == null ? "" : user.getUserName());
            // 订阅信息
            userSubscribeRobotIds = paymentService.getUserSubscribeRobotIds(userId);
        } else {
            user = userService.getDummyUser(dummyUid);
        }

        String userPreferRobotId = getUserPreferRobotId(user);
        List<RobotInfoView> robotInfoViewList = new ArrayList<>();
        // 拿全部的robot
        for (Robot robot : robotService.getAllRobot()) {
            RobotInfoView robotInfoView = RobotInfoView.convertRobot(robot);
            if (userSubscribeRobotIds.contains(robot.getRobotId())) {
                robotInfoView.setUserHasSubscribe(true);
            }
            // 排序，把用户的prefer robot强插到第一位置
            if (StringUtils.isNotEmpty(userPreferRobotId) && robot.getRobotId().equals(userPreferRobotId)) {
                robotInfoViewList.add(0, robotInfoView);
            } else {
                robotInfoViewList.add(robotInfoView);
            }
        }
        feedView.setRobotInfoViewList(robotInfoViewList);

        if (CollectionUtils.isEmpty(robotInfoViewList)) {
            log.error("feed robot get failed, userId={}, dummyUd={}", userId, dummyUid);
            prometheusUtil.perf(PERF_ERROR_MODULE ,"feed_render_failed_by_robot_empty");
        } else {
            prometheusUtil.perf(PERF_FEED_MODULE ,"feed_render_success");
        }
        result.put(DATA, feedView);
        return result;
    }

    private String getUserPreferRobotId(User user) {
        if (user == null || StringUtils.isEmpty(user.getUserPreferInfo()) || !user.getUserPreferInfo().contains(":")) {
            return "";
        }
        return user.getUserPreferInfo().split(":")[0];
    }
}
