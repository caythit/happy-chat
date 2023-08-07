package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.DATA;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.happy.chat.domain.Robot;
import com.happy.chat.domain.User;
import com.happy.chat.model.UserGetRequest;
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

    public Map<String, Object> foryou(String userId, int size) {
        Map<String, Object> result = ApiResult.ofSuccess();

        FeedView feedView = new FeedView();
        // 登录过
        if (StringUtils.isNotEmpty(userId)) {
            // 拿名字
            User user = userService.getUser(UserGetRequest.builder()
                    .userId(userId)
                    .build());
            if (user != null) {
                prometheusUtil.perf("feed_user_get_success");
                feedView.setUserName(user.getUserName());
            } else {
                log.error("feed user get failed, userId={}", userId);
                prometheusUtil.perf("feed_user_get_failed");
            }
        }
        // 拿全部的robot 后面可能会分页
        List<Robot> robotList = robotService.getAllRobot();
        List<RobotInfoView> robotInfoViewList = robotList.stream()
                .map(RobotInfoView::convertRobot)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(robotInfoViewList)) {
            log.error("feed robot get failed, userId={}", userId);
            prometheusUtil.perf("feed_robot_get_failed");
        } else {
            prometheusUtil.perf("feed_robot_get_success");
            feedView.setRobotInfoViewList(robotInfoViewList);
        }
        result.put(DATA, feedView);
        return result;
    }
}
