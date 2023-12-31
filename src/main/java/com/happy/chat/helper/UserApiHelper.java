package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.DATA;
import static com.happy.chat.constants.Constant.PERF_ERROR_MODULE;
import static com.happy.chat.constants.Constant.PERF_USER_MODULE;
import static com.happy.chat.enums.ErrorEnum.SERVER_ERROR;
import static com.happy.chat.uitls.CommonUtils.defaultUserName;
import static com.happy.chat.uitls.EnDecoderUtil.generateSalt;

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
import com.happy.chat.uitls.PrometheusUtils;
import com.happy.chat.view.UserInfoView;

import lombok.extern.slf4j.Slf4j;


@Lazy
@Component
@Slf4j
public class UserApiHelper {
    @Autowired
    private PrometheusUtils prometheusUtil;

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
            log.error("doLoginByEmail user null {}", email);
            prometheusUtil.perf(PERF_USER_MODULE, "登陆失败-输入邮箱不存在");
            return ApiResult.ofFail(ErrorEnum.USER_NOT_EXIST);
        }

        // 解密
        String decodePwd = CommonUtils.decryptPwd(user.getPwdSalt(), user.getUserPwd());
        if (!StringUtils.equals(password, decodePwd)) {
            log.error("doLoginByEmail password not correct {} {}", email, password);
            prometheusUtil.perf(PERF_USER_MODULE, "登陆失败-密码错误");
            return ApiResult.ofFail(ErrorEnum.PASSWORD_ERROR);
        }
        prometheusUtil.perf(PERF_USER_MODULE, "登陆成功");
        UserInfoView userView = UserInfoView.convert(user);
        result.put(DATA, userView);
        return result;
    }

    public Map<String, Object> doRegisterByEmail(String dummyUserId, String email, String password) {
        Map<String, Object> result = ApiResult.ofSuccess();

        ErrorEnum registerErr = registerVerify(email, password);
        if (registerErr != ErrorEnum.SUCCESS) {
            prometheusUtil.perf(PERF_USER_MODULE, "注册失败,错误码：" + registerErr.getErrCode());
            log.error("doRegisterByEmail verify failed {} {}", email, password);
            return ApiResult.ofFail(registerErr);
        }
        User dummyUser = userService.getDummyUser(dummyUserId);
        if (dummyUser == null) {
            prometheusUtil.perf(PERF_USER_MODULE, "注册失败-未找到dummy user信息(Error)");
            prometheusUtil.perf(PERF_ERROR_MODULE, "注册失败-未找到dummy user信息");
            log.error("doRegisterByEmail failed by dummy user not exists {} {}", email, password);
            return ApiResult.ofFail(ErrorEnum.REG_FAILED_BY_DUMMY_NOT_EXIST);
        }

        // 加密
        String salt = generateSalt();
        String encryptPwd = CommonUtils.encryptPwd(salt, password);
        // insert db
        User user = new User();
        // 不用生成uid，使用dummy uid覆盖即可
        user.setUserId(dummyUserId);
        user.setUserName(defaultUserName());
        user.setUserPwd(encryptPwd);
        user.setEmail(email);
        user.setPwdSalt(salt);
        int effectRow = userService.addUser(user);
        if (effectRow <= 0) {
            log.error("doRegisterByEmail verify failed {} {}", email, dummyUserId);
            prometheusUtil.perf(PERF_USER_MODULE, "注册失败-写DB错误(Error)");
            prometheusUtil.perf(PERF_ERROR_MODULE, "注册失败-写DB错误");
            return ApiResult.ofFail(SERVER_ERROR);
        }
        prometheusUtil.perf(PERF_USER_MODULE, "注册成功");
        UserInfoView userView = UserInfoView.convert(user);
        result.put(DATA, userView);
        return result;
    }


    // 简单验证
    private ErrorEnum registerVerify(String email, String password) {
        // 检查email的有效性
        if (!CommonUtils.emailPatternValid(email)) {
            log.error("registerVerify failed by email pattern invalid {}", email);
            return ErrorEnum.EMAIL_PATTERN_INVALID;
        }

        if (!CommonUtils.passwordPatternValid(password)) {
            log.error("registerVerify failed by password pattern invalid {}", password);
            return ErrorEnum.PASSWORD_PATTERN_INVALID;
        }

        User user = userService.getUser(UserGetRequest.builder()
                .email(email)
                .build());
        if (user != null) {
            log.error("registerVerify failed by email already used {}", email);
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
            log.error("{} checkPassword failed by user null {}", purpose, userId);
            prometheusUtil.perf(PERF_USER_MODULE, "密码校验失败-用户不存在,目的: " + purpose);
            return ErrorEnum.USER_NOT_EXIST;
        }
        String decryptPwd = CommonUtils.decryptPwd(user.getPwdSalt(), user.getUserPwd());
        if (!decryptPwd.equals(password)) {
            log.error("{} checkPassword failed password incorrect {}", purpose, userId);
            prometheusUtil.perf(PERF_USER_MODULE, "密码校验失败-密码错误,目的: " + purpose);
            return ErrorEnum.PASSWORD_ERROR;
        }
        prometheusUtil.perf(PERF_USER_MODULE, "密码校验成功,目的: " + purpose);
        return ErrorEnum.SUCCESS;
    }

    //
    public Map<String, Object> modifyPassword(String userId, String pwd) {
        // 检查新密码的格式符合要求
        if (!CommonUtils.passwordPatternValid(pwd)) {
            log.error("modifyPassword failed by pattern invalid {}", userId);
            prometheusUtil.perf(PERF_USER_MODULE, "修改密码失败-密码格式不符合要求");
            return ApiResult.ofFail(ErrorEnum.PASSWORD_PATTERN_INVALID);
        }
        User user = userService.getUser(UserGetRequest.builder()
                .userId(userId)
                .build());
        if (user == null) {
            log.error("modifyPassword failed by user null {}", userId);
            prometheusUtil.perf(PERF_USER_MODULE, "修改密码失败-用户不存在");
            return ApiResult.ofFail(ErrorEnum.USER_NOT_EXIST);
        }

        String encryptPwd = CommonUtils.encryptPwd(user.getPwdSalt(), pwd);
        int effectRow = userService.modifyUserPwd(userId, encryptPwd);
        if (effectRow <= 0) {
            log.error("modifyPassword failed by insert db {}", userId);
            prometheusUtil.perf(PERF_USER_MODULE, "修改密码失败-写DB错误(Error)");
            prometheusUtil.perf(PERF_ERROR_MODULE, "修改密码失败-写DB错误");
            return ApiResult.ofFail(ErrorEnum.RESET_PASSWORD_FAIL);
        }
        prometheusUtil.perf(PERF_USER_MODULE, "修改密码成功");
        return ApiResult.ofSuccess();
    }

    public Map<String, Object> modifyUserName(String userId, String userName) {
        int effectRow = userService.modifyUserName(userId, userName);
        if (effectRow <= 0) {
            log.error("modifyUserName insert db failed {}", userName);
            prometheusUtil.perf(PERF_USER_MODULE, "修改用户名失败-写DB错误(Error)");
            prometheusUtil.perf(PERF_ERROR_MODULE, "修改用户名失败-写DB错误");
            return ApiResult.ofFail(ErrorEnum.REBIND_EMAIL_FAIL);
        }
        prometheusUtil.perf(PERF_USER_MODULE, "修改用户名成功");
        return ApiResult.ofSuccess();

    }

    public Map<String, Object> getUserInfo(String userId) {
        Map<String, Object> result = ApiResult.ofSuccess();

        User user = userService.getUser(UserGetRequest.builder()
                .userId(userId)
                .build());

        // 没有拿到
        if (user == null) {
            log.error("getUserInfo null {}", userId);
            prometheusUtil.perf(PERF_USER_MODULE, "profile未读取到用户信息");
            return ApiResult.ofFail(ErrorEnum.USER_NOT_EXIST);
        }
        UserInfoView userView = UserInfoView.convert(user);
        result.put(DATA, userView);
        prometheusUtil.perf(PERF_USER_MODULE, "profile获取成功");
        return result;
    }
}
