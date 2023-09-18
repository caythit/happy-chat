package com.happy.chat.constants;

public class Constant {
    public static final String ERROR_CODE = "err_code";
    public static final String ERROR_MESSAGE = "err_msg";
    public static final String ERROR_URL = "err_url";
    public static final String DATA = "data";

    public static final String USER_ID_PREFIX = "usr";
    public static final String ROBOT_ID_PREFIX = "rb";
    public static final String MESSAGE_ID_PREFIX = "mes";
    public static final String PAYMENT_ID_PREFIX = "pmt";

    public static final Integer MIN_USERNAME_LENGTH = 5;
    public static final Integer MAX_USERNAME_LENGTH = 20;
    public static final Integer MIN_USER_PWD_LENGTH = 8;
    public static final Integer MAX_USER_PWD_LENGTH = 16;

    public static final String COOKIE_SESSION_ID = "sessionId";
    public static final String EXTRA_INFO_ROBOT_WORK = "work";
    public static final String EXTRA_INFO_ROBOT_INTEREST = "interest";
    public static final String EXTRA_INFO_ROBOT_ABOUT_ME = "aboutMe";
    public static final String EXTRA_INFO_MESSAGE_PAY_TIPS = "payTips";
    public static final String EXTRA_INFO_MESSAGE_SYSTEM_TIPS = "systemTips";

    public static final String CHAT_FROM_USER = "user";
    public static final String CHAT_FROM_ROBOT = "robot";

    public static final String PERF_STARTUP_MODULE = "startup";
    public static final String PERF_HTTP_MODULE = "okhttp";
    public static final String PERF_USER_MODULE = "user";
    public static final String PERF_ROBOT_MODULE = "robot";
    public static final String PERF_PAYMENT_MODULE = "payment";
    public static final String PERF_FEED_MODULE = "feed";
    public static final String PERF_EMAIL_MODULE = "email";
    public static final String PERF_CHAT_MODULE = "chat";

    // 必须重点关注，比如写db失败等不符合预期的中断错误
    public static final String PERF_ERROR_MODULE = "error";


}
