package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.DATA;
import static com.happy.chat.uitls.PrometheusUtils.perf;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.happy.chat.domain.Robot;
import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.service.RobotService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.view.RobotInfoView;

import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;


@Lazy
@Component
@Slf4j
public class RobotApiHelper {
    private final String prometheusName = "robot";
    private final String prometheusHelp = "AI请求";

    @Autowired
    private CollectorRegistry robotRegistry;

    @Autowired
    private RobotService robotService;

    public Map<String, Object> getRobotProfile(String robotId) {
        Robot robot = robotService.getRobotById(robotId);
        if (robot == null) {
            log.error("getRobotProfile failed, robotId={}", robotId);
            perf(robotRegistry, prometheusName, prometheusHelp, "robot_get_failed", robotId);
            return ApiResult.ofFail(ErrorEnum.ROBOT_NOT_EXIST);
        }
        perf(robotRegistry, prometheusName, prometheusHelp, "robot_get_success", robotId);
        Map<String, Object> result = ApiResult.ofSuccess();
        result.put(DATA, RobotInfoView.convertRobot(robot));
        return result;
    }
}
