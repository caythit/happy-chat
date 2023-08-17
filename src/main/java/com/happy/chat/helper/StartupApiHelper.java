package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.USER_ID_PREFIX;
import static com.happy.chat.uitls.CacheKeyProvider.startupConfigKey;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.happy.chat.domain.Robot;
import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.model.StartupConfigModel;
import com.happy.chat.service.RobotService;
import com.happy.chat.service.UserService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.CommonUtils;
import com.happy.chat.uitls.ObjectMapperUtils;
import com.happy.chat.uitls.PrometheusUtils;
import com.happy.chat.uitls.RedisUtil;
import com.happy.chat.view.StartupConfigView;
import com.happy.chat.view.StartupConfigView.RobotStartupView;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Component
@Slf4j
public class StartupApiHelper {

    @Autowired
    private PrometheusUtils prometheusUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RobotService robotService;

    public StartupConfigView getConfig() {
        // 优先从redis读(提前写进去)，如果没有直接使用mock写死的数据
        String str = redisUtil.get(startupConfigKey());
        StartupConfigModel model = ObjectMapperUtils.fromJSON(str, StartupConfigModel.class);

        StartupConfigView startupConfigView = new StartupConfigView();
        if (model == null) {
            log.error("getConfig failed, use mock view");
            prometheusUtil.perf("mock_config");
            startupConfigView = mockView();
        } else {
            prometheusUtil.perf("read_config");
            startupConfigView.setLogoUrl(model.getLogoUrl());
            startupConfigView.setAgeOptions(model.getAgeOptions());
            startupConfigView.setIntroduceText(model.getIntroduceText());
            startupConfigView.setWelcomeText(model.getWelcomeText());
            Map<String, Robot> robotMap = robotService.batchGetRobotById(model.getPymlRobotIds());
            startupConfigView.setPymlRobots(robotMap.values().stream()
                    .map(r -> new RobotStartupView(r.getRobotId(), r.getHeadUrl(), r.getName()))
                    .collect(Collectors.toList()));
        }

        // 生成dummy user
        String dummyUserId = CommonUtils.uuid(USER_ID_PREFIX);
        int effectRow = userService.addDummyUser(dummyUserId);
        if (effectRow <= 0) {
            log.error("insert db dummy user failed {}", dummyUserId);
            prometheusUtil.perf("add_dummy_user_failed");
        }
        startupConfigView.setDummyUid(dummyUserId);
        return startupConfigView;
    }


    private StartupConfigView mockView() {
        StartupConfigView startupConfigView = new StartupConfigView();
        // TODO
        startupConfigView.setLogoUrl("");
        startupConfigView.setAgeOptions(ImmutableList.of("18-25", "25-35", "35+"));
        startupConfigView.setIntroduceText("Hello!\n who would you like\n to chat with?");
        startupConfigView.setWelcomeText("How are you \ntoday?");
        // TODO
        startupConfigView.setPymlRobots(ImmutableList.<RobotStartupView>builder()
                .add(new RobotStartupView("", "", ""))
                .add(new RobotStartupView("", "", ""))
                .add(new RobotStartupView("", "", ""))
                .add(new RobotStartupView("", "", ""))
                .add(new RobotStartupView("", "", ""))
                .add(new RobotStartupView("", "", ""))
                .build());
        return startupConfigView;
    }


    public Map<String, Object> recordUserPrefer(String userId, String preferRobotId, String agePrefer) {
        if (StringUtils.isEmpty(preferRobotId) && StringUtils.isEmpty(agePrefer)) {
            log.warn("preferRobotId and agePrefer empty");
            return ApiResult.ofSuccess();
        }
        String preferInfo = String.format("%s:%s", preferRobotId, agePrefer);
        int effectRow = userService.updateUserPreferInfo(userId, preferInfo);
        if (effectRow <= 0) {
            // 打点
            log.error("recordUserPrefer failed {} {} {}", userId, preferRobotId, agePrefer);
            prometheusUtil.perf("user_prefer_info_failed");
            return ApiResult.ofFail(ErrorEnum.SERVER_ERROR);
        }
        // 打点
        prometheusUtil.perf("user_prefer_info_success");
        return ApiResult.ofSuccess();
    }
}
