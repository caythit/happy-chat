package com.happy.chat.uitls;

public class CacheKeyProvider {

    private static final String PREFIX = "flirtopia:";

    public static String startupConfigKey() {
        return PREFIX.concat("startConfig");
    }

    public static String mailCodeKey(String email) {
        String key = String.format("mailCode:%s", email);
        return PREFIX.concat(key);
    }

    // robot兜底回复
    public static String defaultRobotRespChatKey() {
        return PREFIX.concat("defaultRobotRespChat");
    }

    // 聊天敏感词
    public static String chatSensitiveWordKey() {
        return PREFIX.concat("chatSensitiveWord");
    }

    public static String chatUnPayWordKey() {
        return PREFIX.concat("chatUnPayWord");
    }

    public static String chatWarnWordKey() {
        return PREFIX.concat("chatWarnWordKey");
    }

    public static String chatFinishPayWordKey() {
        return PREFIX.concat("chatFinishPayWord");
    }

    public static String userEnterHappyModelLatestTimeKey(String userId, String robotId) {
        return PREFIX.concat(String.format("userEnterHappyModelLatestTime:%s:%s", userId, robotId));
    }


    public static String userExitHappyModelExpireMillsKey() {
        return PREFIX.concat("userExitHappyModelExpireMills");
    }

    // 3次
    public static String userChatgptWarnMaxCountKey() {
        return PREFIX.concat("userChatgptWarnMaxCount");
    }

    // 10
    public static String userEnterChatgptAdvanceModelThresholdKey() {
        return PREFIX.concat("userEnterChatgptAdvanceModelThreshold");
    }

    public static String userChatgptWarnKey(String userId, String robotId) {
        return PREFIX.concat(String.format("userChatgptWarn:%s:%s", userId, robotId));
    }

    public static String robotGptPromptKey(String robotId, String version) {
        return PREFIX.concat(String.format("robotGptPrompt:%s:%s", robotId, version));
    }
}
