package com.happy.chat.domain;

import lombok.Data;

@Data
public class Robot {
    private String robotId;
    private String name;
    private int sex; // 0-man、1-woman
    private int age;
    private String city;
    private String country;
    private String headUrl;
    private String coverUrl;
    private String bgUrl;
    private String extraInfo;
}
