package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.DATA;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.happy.chat.domain.Robot;
import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.service.PaymentService;
import com.happy.chat.service.RobotService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.PrometheusUtils;
import com.happy.chat.view.RobotInfoView;

import lombok.extern.slf4j.Slf4j;


@Lazy
@Component
@Slf4j
public class RobotApiHelper {
    @Autowired
    private PrometheusUtils prometheusUtil;

    @Autowired
    private RobotService robotService;

    @Autowired
    private PaymentService paymentService;

    public Map<String, Object> getRobotProfile(String userId, String robotId) {
        Robot robot = robotService.getRobotById(robotId);
        if (robot == null) {
            log.error("getRobotProfile failed, robotId={}", robotId);
            prometheusUtil.perf("robot_get_failed_" + robotId);
            return ApiResult.ofFail(ErrorEnum.ROBOT_NOT_EXIST);
        }
        prometheusUtil.perf("robot_get_success");
        Map<String, Object> result = ApiResult.ofSuccess();

        RobotInfoView robotInfoView = RobotInfoView.convertRobot(robot);
        if (StringUtils.isNotEmpty(userId)) {
            robotInfoView.setUserHasSubscribe(paymentService.userHasPayedRobot(userId, robotId));
        }
        result.put(DATA, robotInfoView);
        return result;
    }
}
