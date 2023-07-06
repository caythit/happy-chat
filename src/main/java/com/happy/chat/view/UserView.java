package com.happy.chat.view;

import com.happy.chat.model.User;

import lombok.Data;

/**
 * 返回上层用的
 */
@Data
public class UserView {
    private String userId;
    private String userName;
    private String email;
    private String phone;
    private String extraInfo;

    public static UserView convert(User user) {
        UserView userView = new UserView();
        userView.setUserId(user.getUserId());
        userView.setUserName(user.getUserName());
        userView.setEmail(user.getEmail());
        userView.setPhone(user.getPhone());
        userView.setExtraInfo(user.getExtraInfo());
        return userView;
    }
}
