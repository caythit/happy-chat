package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.DATA;
import static com.happy.chat.enums.ErrorEnum.SERVER_ERROR;
import static com.happy.chat.uitls.CommonUtils.defaultUserName;
import static com.happy.chat.uitls.EnDecoderUtil.generateSalt;
import static com.happy.chat.uitls.PrometheusUtils.perf;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.happy.chat.domain.User;
import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.model.UserGetRequest;
import com.happy.chat.service.UserService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.CommonUtils;
import com.happy.chat.view.UserInfoView;

import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;


@Lazy
@Component
@Slf4j
public class UserApiHelper {
    private final String prometheusName = "user";
    private final String prometheusHelp = "用户请求";

    @Autowired
    private CollectorRegistry userRegistry;

    @Autowired
    private UserService userService;

    public Map<String, Object> doLoginByEmail(String email, String password) {
        Map<String, Object> result = ApiResult.ofSuccess();

        // 根据email直接去db拿数据
        User user = userService.getUser(UserGetRequest.builder()
                .email(email)
                .build());

        // 没有拿到
        if (user == null) {
            perf(userRegistry, prometheusName, prometheusHelp, "login_failed_by_not_exist", email);
            return ApiResult.ofFail(ErrorEnum.USER_NOT_EXIST);
        }

        // 解密
        String decodePwd = CommonUtils.decryptPwd(user.getPwdSalt(), user.getUserPwd());
        if (!StringUtils.equals(password, decodePwd)) {
            perf(userRegistry, prometheusName, prometheusHelp, "login_failed_by_pwd_error", email);
            return ApiResult.ofFail(ErrorEnum.PASSWORD_ERROR);
        }

        perf(userRegistry, prometheusName, prometheusHelp, "login_success", email);
        UserInfoView userView = UserInfoView.convert(user);
        result.put(DATA, userView);
        return result;
    }

    public Map<String, Object> doRegisterByEmail(String dummyUserId, String userName, String email, String password) {
        Map<String, Object> result = new HashMap<>();

        ErrorEnum registerErr = registerVerify(userName, password);
        if (registerErr != ErrorEnum.SUCCESS) {
            return ApiResult.ofFail(registerErr);
        }
        // 加密
        String salt = generateSalt();
        String encryptPwd = CommonUtils.encryptPwd(salt, password);
        // insert db
        User user = new User();
        // 不用生成uid，使用dummy uid覆盖即可
        user.setUserId(dummyUserId);
        user.setUserName(StringUtils.isEmpty(userName) ? defaultUserName() : userName);
        user.setUserPwd(encryptPwd);
        user.setEmail(email);
        user.setPwdSalt(salt);
        int effectRow = userService.addUser(user);
        if (effectRow <= 0) {
            perf(userRegistry, prometheusName, prometheusHelp, "reg_failed_by_server_error", email);
            return ApiResult.ofFail(SERVER_ERROR);
        }
        perf(userRegistry, prometheusName, prometheusHelp, "reg_success", email);
        UserInfoView userView = UserInfoView.convert(user);
        result.put(DATA, userView);
        return result;
    }


    // 简单验证
    private ErrorEnum registerVerify(String email, String password) {
        // 检查email的有效性
        if (!CommonUtils.emailPatternValid(email)) {
            perf(userRegistry, prometheusName, prometheusHelp, "reg_failed_by_email_invalid", email);
            return ErrorEnum.EMAIL_PATTERN_INVALID;
        }

        if (!CommonUtils.passwordPatternValid(password)) {
            perf(userRegistry, prometheusName, prometheusHelp, "reg_failed_by_pwd_invalid", email);
            return ErrorEnum.PASSWORD_PATTERN_INVALID;
        }

        User user = userService.getUser(UserGetRequest.builder()
                .email(email)
                .build());
        if (user != null) {
            perf(userRegistry, prometheusName, prometheusHelp, "reg_failed_by_user_exist", email);
            return ErrorEnum.USER_EMAIL_ALREADY_USED;
        }
        return ErrorEnum.SUCCESS;
    }

    // 检查密码是否正确
    // 用于修改密码以及修改邮箱 之前的校验
    public ErrorEnum checkPassword(String userId, String password, String purpose) {
        User user = userService.getUser(UserGetRequest.builder()
                .userId(userId)
                .build());
        if (user == null) {
            perf(userRegistry, prometheusName, prometheusHelp, "check_password_failed_by_user_not_exist", purpose, userId);
            return ErrorEnum.USER_NOT_EXIST;
        }
        String decryptPwd = CommonUtils.decryptPwd(user.getPwdSalt(), user.getUserPwd());
        if (!decryptPwd.equals(password)) {
            perf(userRegistry, prometheusName, prometheusHelp, "check_password_failed_by_pwd_error", purpose, userId);
            return ErrorEnum.PASSWORD_ERROR;
        }
        perf(userRegistry, prometheusName, prometheusHelp, "check_password_success", purpose, userId);
        return ErrorEnum.SUCCESS;
    }

    // 修改邮箱最后一步-重新绑定新邮箱(不用校验格式，已经校验过了)
    public Map<String, Object> rebindEmail(String userId, String email) {
        int effectRow = userService.rebindEmail(userId, email);
        if (effectRow <= 0) {
            perf(userRegistry, prometheusName, prometheusHelp, "rebind_email_failed_by_db_error", userId, email);
            return ApiResult.ofFail(ErrorEnum.REBIND_EMAIL_FAIL);
        }
        perf(userRegistry, prometheusName, prometheusHelp, "rebind_email_success", userId, email);
        return ApiResult.ofSuccess();
    }


    // 用于修改密码和忘记密码的最后一步-重置密码
    public Map<String, Object> resetPassword(String userId, String pwd, String purpose) {
        // 检查新密码的格式符合要求
        if (!CommonUtils.passwordPatternValid(pwd)) {
            perf(userRegistry, prometheusName, prometheusHelp, "reset_password_failed_by_pattern_invalid", userId, purpose);
            return ApiResult.ofFail(ErrorEnum.PASSWORD_PATTERN_INVALID);
        }
        User user = userService.getUser(UserGetRequest.builder()
                .userId(userId)
                .build());
        if (user == null) {
            perf(userRegistry, prometheusName, prometheusHelp, "reset_password_failed_by_user_not_exist", userId, purpose);
            return ApiResult.ofFail(ErrorEnum.USER_NOT_EXIST);
        }

        String encryptPwd = CommonUtils.encryptPwd(user.getPwdSalt(), pwd);
        int effectRow = userService.resetUserPwd(userId, encryptPwd);
        if (effectRow <= 0) {
            perf(userRegistry, prometheusName, prometheusHelp, "reset_password_failed_by_db_error", userId, purpose);
            return ApiResult.ofFail(ErrorEnum.RESET_PASSWORD_FAIL);
        }
        perf(userRegistry, prometheusName, prometheusHelp, "reset_password_success", userId, purpose);
        return ApiResult.ofSuccess();
    }
}
