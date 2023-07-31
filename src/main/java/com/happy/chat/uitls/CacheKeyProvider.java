package com.happy.chat.uitls;

public class CacheKeyProvider {

    private static final String PREFIX = "flirtopia:";

    public static String startupConfigKey() {
        return PREFIX.concat("stcf");
    }

    public static String mailCodeKey(String email) {
        String key = String.format("mc:%s", email);
        return PREFIX.concat(key);
    }

    // robot兜底回复
    public static String defaultRobotRespChatKey() {
        return PREFIX.concat("drrck");
    }

    // 聊天敏感词
    public static String chatSensitiveWordKey() {
        return PREFIX.concat("cswd");
    }

    public static String chatUnPayWordKey() {
        return PREFIX.concat("cupw");
    }

    public static String chatFinishPayWordKey() {
        return PREFIX.concat("cfpw");
    }
}
