package com.happy.chat.service;

import com.happy.chat.domain.User;
import com.happy.chat.model.UserGetRequest;

public interface UserService {
    User getUser(UserGetRequest userGetRequest);
    User getDummyUser(String dummyUid);

    int addUser(User user);

    int addDummyUser(String userId);

    int updateUserPreferInfo(String userId, String preferRobotId);

    int modifyUserPwd(String userId, String encryptPwd);
    int resetUserPwd(String email, String salt, String encryptPwd);

    int rebindEmail(String userId, String email);
    int modifyUserName(String userId, String userName);

}
