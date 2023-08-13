package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.DATA;

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
import com.happy.chat.service.RobotService;
import com.happy.chat.service.UserService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.ObjectMapperUtils;
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

    public Map<String, Object> foryou(String dummyUid, int size) {
        Map<String, Object> result = ApiResult.ofSuccess();

        FeedView feedView = new FeedView();
        // 拿名字
        User user = userService.getDummyUser(dummyUid);
        if (user != null && StringUtils.isNotEmpty(user.getUserName())) {
            feedView.setUserName(user.getUserName());
        }
        List<RobotInfoView> robotInfoViewList = new ArrayList<>();
        // 先把用户的prefer robot强插到第一位置
        RobotInfoView preferRobot = getUserPreferRobot(user);
        if (preferRobot != null) {
            robotInfoViewList.add(preferRobot);
        }
        // 拿全部的robot 后面可能会分页
        List<Robot> robotList = robotService.getAllRobot();
        robotList.forEach(robot -> {
            // 已经有了 直接跳过
            if (preferRobot != null && robot.getRobotId().equals(preferRobot.getRobotId())) {
                return;
            }
            robotInfoViewList.add(RobotInfoView.convertRobot(robot));
        });
        if (CollectionUtils.isEmpty(robotInfoViewList)) {
            log.error("feed robot get failed, userId={}", dummyUid);
            prometheusUtil.perf("feed_robot_get_failed");
        } else {
            prometheusUtil.perf("feed_robot_get_success");
            feedView.setRobotInfoViewList(robotInfoViewList);
        }
        result.put(DATA, feedView);
        return result;
    }

    private RobotInfoView getUserPreferRobot(User user) {
        if (user == null || StringUtils.isEmpty(user.getUserPreferInfo()) || !user.getUserPreferInfo().contains(":")) {
            return null;
        }
        String robotId = user.getUserPreferInfo().split(":")[0];
        Robot robot = robotService.getRobotById(robotId);
        if (robot == null) {
            log.error("feed robot get failed, robotId={}", robotId);
            prometheusUtil.perf("feed_prefer_robot_get_failed");
            return null;
        }
        return RobotInfoView.convertRobot(robot);
    }
}
