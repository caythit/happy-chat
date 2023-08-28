package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.DATA;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.happy.chat.helper.StartupApiHelper;
import com.happy.chat.uitls.ApiResult;

@RestController
@RequestMapping("/rest/h/startup")
public class StartupController {

    @Autowired
    private StartupApiHelper startupApiHelper;

    /**
     * 兴趣选择
     *
     * @return
     */
    @RequestMapping("/selectPrefer")
    public Map<String, Object> selectPrefer(@RequestParam(value = "ud") String dummyUid,
                                            @RequestParam(value = "robotPrefer", defaultValue = "") String preferRobotId,
                                            @RequestParam(value = "agePrefer", defaultValue = "") String agePrefer) {
        return startupApiHelper.recordUserPrefer(dummyUid, preferRobotId, agePrefer);
    }

    /**
     * 全局配置，包括弹窗等行为
     *
     * @return
     */
    @RequestMapping("/globalConfig")
    public Map<String, Object> globalConfig(@RequestParam(value = "ud", required = false) String dummyUid,
                                            @RequestParam(value = "appver", required = false) String appver) {
        Map<String, Object> res = ApiResult.ofSuccess();
        // 返回相关配置
        res.put(DATA, startupApiHelper.getGlobalConfig(dummyUid, appver));

        return res;
    }

    /**
     * 新用户才有，第一次请求
     *
     * @return
     */
    @RequestMapping("/config")
    public Map<String, Object> newUserConfig() {
        Map<String, Object> res = ApiResult.ofSuccess();
        // 返回相关配置
        res.put(DATA, startupApiHelper.getConfig());

        return res;
    }
}
