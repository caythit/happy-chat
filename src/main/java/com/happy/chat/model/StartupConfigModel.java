package com.happy.chat.model;

import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class StartupConfigModel {
    private String introduceText; // Hello, who would you like to chat with?
    private Set<String> pymlRobotIds;
    private List<String> ageOptions; // 年纪的偏好
    private String welcomeText; // How are you today?
}
