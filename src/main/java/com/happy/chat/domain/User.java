package com.happy.chat.domain;

import lombok.Data;

@Data
public class User {
    private String userId;
    private String userPreferInfo;
    private String userName;
    private String userPwd;
    private String email;
    private String phone;
    private String pwdSalt;
    private String extraInfo;
    private long createTime;
    private long updateTime;
}
