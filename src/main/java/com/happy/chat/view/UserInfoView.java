package com.happy.chat.view;

import com.happy.chat.domain.User;

import lombok.Data;

/**
 * 返回上层用的
 */
@Data
public class UserInfoView {
    private String userId;
    private String userName;
    private String email;
    private String phone;
    private String extraInfo;

    public static UserInfoView convert(User user) {
        UserInfoView userView = new UserInfoView();
        userView.setUserId(user.getUserId());
        userView.setUserName(user.getUserName());
        userView.setEmail(user.getEmail());
        userView.setPhone(user.getPhone());
        userView.setExtraInfo(user.getExtraInfo());
        return userView;
    }
}
