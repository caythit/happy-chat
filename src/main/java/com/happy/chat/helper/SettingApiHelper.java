package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.PERF_ERROR_MODULE;
import static com.happy.chat.constants.Constant.PERF_SETTING_MODULE;
import static com.happy.chat.uitls.EnDecoderUtil.generateSalt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.service.UserService;
import com.happy.chat.uitls.CommonUtils;
import com.happy.chat.uitls.PrometheusUtils;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Component
@Slf4j
public class SettingApiHelper {
    @Autowired
    private PrometheusUtils prometheusUtil;

    @Autowired
    private UserService userService;

    public ErrorEnum forgotResetPassword(String email, String pwd) {
        // 检查新密码的格式符合要求
        if (!CommonUtils.passwordPatternValid(pwd)) {
            log.error("forgotResetPassword failed by pattern invalid {}", email);
            prometheusUtil.perf(PERF_SETTING_MODULE, "reset_pwd_failed_by_pattern_invalid");
            return ErrorEnum.PASSWORD_PATTERN_INVALID;
        }

        // 生成新盐
        String salt = generateSalt();
        String encryptPwd = CommonUtils.encryptPwd(salt, pwd);
        int effectRow = userService.resetUserPwd(email, salt, encryptPwd);
        if (effectRow <= 0) {
            log.error("forgotResetPassword failed by insert db {}", email);
            prometheusUtil.perf(PERF_SETTING_MODULE, "reset_pwd_failed_by_db_error");
            prometheusUtil.perf(PERF_ERROR_MODULE, "reset_pwd_failed_by_db_error");
            return ErrorEnum.RESET_PASSWORD_FAIL;
        }
        return ErrorEnum.SUCCESS;
    }

    // 修改邮箱最后一步-重新绑定新邮箱(不用校验格式，已经校验过了)
    public ErrorEnum rebindEmail(String userId, String email) {
        int effectRow = userService.rebindEmail(userId, email);
        if (effectRow <= 0) {
            log.error("rebindEmail insert db failed {} {}", userId, email);
            prometheusUtil.perf(PERF_SETTING_MODULE, "rebind_email_failed_by_db_error");
            prometheusUtil.perf(PERF_ERROR_MODULE, "rebind_email_failed_by_db_error");
            return ErrorEnum.REBIND_EMAIL_FAIL;
        }
        return ErrorEnum.SUCCESS;
    }
}
