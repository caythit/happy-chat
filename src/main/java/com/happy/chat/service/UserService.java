package com.happy.chat.service;

import com.happy.chat.domain.User;
import com.happy.chat.model.UserGetRequest;

public interface UserService {
    User getUser(UserGetRequest userGetRequest);

    int addUser(User user);

    int addDummyUser(String userId);

    int updateUserPreferInfo(String userId, String preferRobotId);

    int resetUserPwd(String userId, String encryptPwd);

    int rebindEmail(String userId, String email);

}
