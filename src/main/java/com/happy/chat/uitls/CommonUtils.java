package com.happy.chat.uitls;

import java.util.Base64;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

public class CommonUtils {
    public static String uuid() {
        return NanoIdUtils.randomNanoId();
    }

    public static String encryptPwd(String salt, String pwd) {
        return Base64.getEncoder().encodeToString(EnDecoderUtil.DESEncrypt(salt, pwd));
    }

    public static String decryptPwd(String salt, String pwd) {
        return new String(EnDecoderUtil.DESDecrypt(salt, Base64.getDecoder().decode(pwd)));
    }

    public static void main(String[] args) {
        String salt = EnDecoderUtil.generateSalt();
        String enPwd = encryptPwd(salt, "1111");
        String dePwd = decryptPwd(salt, enPwd);

        System.out.println(enPwd);
        System.out.println(dePwd);
    }
}
