package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.PERF_ERROR_MODULE;
import static com.happy.chat.constants.Constant.PERF_STARTUP_MODULE;
import static com.happy.chat.constants.Constant.USER_ID_PREFIX;
import static com.happy.chat.uitls.CacheKeyProvider.startupConfigKey;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import com.happy.chat.view.GlobalConfigView;
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
            prometheusUtil.perf(PERF_STARTUP_MODULE, "新用户redis未配置，使用mock数据");
            model = mockModel();
        }
        startupConfigView.setAgeOptions(model.getAgeOptions());
        startupConfigView.setIntroduceText(model.getIntroduceText());
        startupConfigView.setWelcomeText(model.getWelcomeText());
        Map<String, Robot> robotMap = robotService.batchGetRobotById(model.getPymlRobotIds());
        startupConfigView.setPymlRobots(robotMap.values().stream()
                .map(r -> new RobotStartupView(r.getRobotId(), r.getHeadUrl(), r.getName()))
                .collect(Collectors.toList()));

        // 生成dummy user
        String dummyUserId = CommonUtils.uuid(USER_ID_PREFIX);
        int effectRow = userService.addDummyUser(dummyUserId);
        if (effectRow <= 0) {
            log.error("insert db dummy user failed {}", dummyUserId);
            prometheusUtil.perf(PERF_STARTUP_MODULE, "新用户配置写DB失败(Error)");
            prometheusUtil.perf(PERF_ERROR_MODULE, "新用户配置写DB失败");
        } else {
            startupConfigView.setDummyUid(dummyUserId);
            prometheusUtil.perf(PERF_STARTUP_MODULE, "新用户配置下发成功");
        }
        return startupConfigView;
    }


    private StartupConfigModel mockModel() {
        StartupConfigModel startupConfig = new StartupConfigModel();
        startupConfig.setAgeOptions(ImmutableList.of("18-25", "25-35", "35+"));
        startupConfig.setIntroduceText("Who would you like to chat with?");
        startupConfig.setWelcomeText("How are you \ntoday?");
        startupConfig.setPymlRobotIds(ImmutableSet.<String>builder()
                .add("rb_test8")
                .add("rb_test4")
                .add("rb_test11")
                .add("rb_test5")
                .add("rb_test10")
                .add("rb_test2")
                .build());
        return startupConfig;
    }


    public Map<String, Object> recordUserPrefer(String userId, String preferRobotId, String agePrefer) {
        if (StringUtils.isEmpty(preferRobotId) && StringUtils.isEmpty(agePrefer)) {
            prometheusUtil.perf(PERF_STARTUP_MODULE, "用户跳过兴趣选择");
            return ApiResult.ofSuccess();
        }
        String preferInfo = String.format("%s:%s", preferRobotId, agePrefer);
        int effectRow = userService.updateUserPreferInfo(userId, preferInfo);
        if (effectRow <= 0) {
            log.error("recordUserPrefer failed {} {} {}", userId, preferRobotId, agePrefer);
            prometheusUtil.perf(PERF_STARTUP_MODULE, "用户兴趣选择写DB失败(Error)");
            prometheusUtil.perf(PERF_ERROR_MODULE, "用户兴趣选择写DB失败");
            return ApiResult.ofFail(ErrorEnum.SERVER_ERROR);
        }
        prometheusUtil.perf(PERF_STARTUP_MODULE, "用户兴趣选择处理成功");
        return ApiResult.ofSuccess();
    }

    public GlobalConfigView getGlobalConfig(String dummyUid, String appver) {
        GlobalConfigView view = new GlobalConfigView();

        GlobalConfigView.UpdateDialog updateDialog = new GlobalConfigView.UpdateDialog();
        view.setUpdateDialog(updateDialog);
        prometheusUtil.perf(PERF_STARTUP_MODULE, "全局配置下发成功");
        return view;
    }
}
