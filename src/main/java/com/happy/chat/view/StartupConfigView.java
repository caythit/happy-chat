package com.happy.chat.view;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
public class StartupConfigView {
    // 字段没用 写在客户端
    private String logoUrl;
    private String introduceText; // Hello, who would you like to chat with?
    private List<RobotStartupView> pymlRobots;
    private List<String> ageOptions; // 年纪的偏好
    private String welcomeText; // How are you today?
    @JsonProperty("ud")
    private String dummyUid;

    @Data
    public static class RobotStartupView {
        private String robotId;
        private String headUrl;
        private String name;

        public RobotStartupView(String robotId, String headUrl, String name) {
            this.robotId = robotId;
            this.headUrl = headUrl;
            this.name = name;
        }
    }
}



