package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.USER_ID_PREFIX;
import static com.happy.chat.uitls.CacheKeyProvider.startupConfigKey;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.happy.chat.enums.ErrorEnum;
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

    public StartupConfigView getConfig() {
        // 优先从redis读(提前写进去)，如果没有直接使用mock写死的数据
        String str = redisUtil.get(startupConfigKey());
        StartupConfigView view = ObjectMapperUtils.fromJSON(str, StartupConfigView.class);

        if (view == null) {
            log.error("getConfig failed, use mock view");
            prometheusUtil.perf("mock_config");
            view = mockView();
        } else {
            prometheusUtil.perf("read_config");
        }

        // 生成dummy user
        String dummyUserId = CommonUtils.uuid(USER_ID_PREFIX);
        int effectRow = userService.addDummyUser(dummyUserId);
        if (effectRow <= 0) {
            log.error("insert db dummy user failed {}", dummyUserId);
            prometheusUtil.perf("add_dummy_user_failed");
        }
        view.setDummyUid(dummyUserId);
        return view;
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
