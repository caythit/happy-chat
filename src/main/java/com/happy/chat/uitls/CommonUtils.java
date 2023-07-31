package com.happy.chat.uitls;

import static com.happy.chat.constants.Constant.MAX_USER_PWD_LENGTH;
import static com.happy.chat.constants.Constant.MIN_USER_PWD_LENGTH;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtils {

    public static String defaultUserName() {
        return "User_" + System.currentTimeMillis();
    }

    public static String uuid(String prefix) {
        return prefix + "_" + NanoIdUtils.randomNanoId();
    }

    public static String encryptPwd(String salt, String pwd) {
        return Base64.getEncoder().encodeToString(EnDecoderUtil.DESEncrypt(salt, pwd));
    }

    public static String decryptPwd(String salt, String pwd) {
        return new String(EnDecoderUtil.DESDecrypt(salt, Base64.getDecoder().decode(pwd)));
    }

    public static String formatMessageWithArgs(String message, Object... args) {
        if (args != null && args.length > 0) {
            try {
                if (StringUtils.contains(message, "%s")) {
                    return String.format(message, args);
                } else if (StringUtils.contains(message, "${0}")) {
                    // 新版文案方式, 用${0} ${1} ... 代替%s，与客户端文案一致, 方便翻译平台统一处理
                    Map<String, Object> params = new HashMap<>();
                    for (int i = 0; i < args.length; i++) {
                        params.put(String.valueOf(i), args[i]);
                    }
                    return StrSubstitutor.replace(message, params);
                } else {
                    message = message + " " + args[0];
                }
            } catch (MissingFormatArgumentException e) {
                log.info("MissingFormatArgumentException: ", e);
            }
        }
        return message;
    }

    public static boolean emailPatternValid(String email) {
        return false; // todo 邮箱正则匹配
    }

    public static boolean passwordPatternValid(String password) {
        if (password.length() < MIN_USER_PWD_LENGTH) {
            return false;
        }
        if (password.length() > MAX_USER_PWD_LENGTH) {
            return false;
        }
        return true; // todo 密码验证组合
    }

    public static int randomMailCode() {
        return ThreadLocalRandom.current().nextInt(100000, 999999);
    }

    public static void main(String[] args) {
        String salt = EnDecoderUtil.generateSalt();
        String enPwd = encryptPwd(salt, "1111");
        String dePwd = decryptPwd(salt, enPwd);

        System.out.println(enPwd);
        System.out.println(dePwd);
    }
}
