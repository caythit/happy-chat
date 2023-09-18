package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.DATA;
import static com.happy.chat.constants.Constant.PERF_ERROR_MODULE;
import static com.happy.chat.constants.Constant.PERF_ROBOT_MODULE;

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
            prometheusUtil.perf(PERF_ROBOT_MODULE, "robot_get_failed_" + robotId);
            prometheusUtil.perf(PERF_ERROR_MODULE, "robot_get_failed_" + robotId);
            return ApiResult.ofFail(ErrorEnum.ROBOT_NOT_EXIST);
        }
        Map<String, Object> result = ApiResult.ofSuccess();

        RobotInfoView robotInfoView = RobotInfoView.convertRobot(robot);
        if (StringUtils.isNotEmpty(userId)) {
            robotInfoView.setUserHasSubscribe(paymentService.userHasPayedRobot(userId, robotId));
        }
        result.put(DATA, robotInfoView);
        prometheusUtil.perf(PERF_ROBOT_MODULE, "robot_profile_get_success");
        return result;
    }
}
