package com.happy.chat.uitls;

public class CacheKeyProvider {

    private static final String PREFIX = "flirtopia:";

    public static String startupConfigKey() {
        return PREFIX.concat("startConfig");
    }

    public static String gptApiTokenKey() {
        return PREFIX.concat("gptApiToken");
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

    public static String chatWarnWordKey() {
        return PREFIX.concat("chatWarnWord");
    }

    /**
     * set flirtopia:chatSystemTips
     * -Don't scare a girl. Do it gently and softly.
     * @return
     */
    public static String chatSystemTipsKey() {
        return PREFIX.concat("chatSystemTips");
    }

    /**
     * lpush flirtopia:chatUnPayTips
     * <p>
     * -Don’t be shy to undress me, only $9.9
     * -Darling i want to serve you better, only $9.9
     * -$9.9 for R-18 experience.
     * -I'll show you my true collor, only $9.9
     * -Trying to get to second base with me? Only $9.9
     * -You want me to give you a tease? Only $9.9
     *
     * @return
     */
    public static String chatUnPayTipsKey() {
        return PREFIX.concat("chatUnPayTips");
    }

    /**
     * lpush flirtopia:chatFinishPayTips
     * <p>
     * -I will only serve you at all time.
     * -I want you so bad.
     * -I am thirsty to have you.
     * -I love you. Are you rich in pants?
     * -Let's have a short time but good time
     * -Give me a spanking now!
     * -I'm so turned on!
     *
     * @return
     */
    public static String chatFinishPayTipsKey() {
        return PREFIX.concat("chatFinishPayTips");
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

    public static String robotStripePriceIdKey(String robotId) {
        return PREFIX.concat(String.format("robotStripePriceId:%s", robotId));
    }

    public static String happyModelHttpUrl() {
        return PREFIX.concat("happyModelHttpUrl");
    }

    public static String stripeApiKeySalt() {
        return PREFIX.concat("stripeApiKeySalt");
    }

    public static String stripeWebhookSecretSalt() {
        return PREFIX.concat("stripeWebhookSecretSalt");
    }

    public static void main(String[] args) {
        String fileName = String.format("prompt/%s_%s.prompt", "rb_test11", "normal");
        String prompt = FileUtils.getFileContent(fileName);
        System.out.println(prompt);
    }
}
