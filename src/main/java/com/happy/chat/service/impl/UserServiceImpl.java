package com.happy.chat.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.happy.chat.dao.UserDao;
import com.happy.chat.domain.User;
import com.happy.chat.model.UserGetRequest;
import com.happy.chat.service.UserService;

@Lazy
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    public User getUser(UserGetRequest request) {
        if (StringUtils.isNotEmpty(request.getUserId())) {
            return userDao.getUserById(request.getUserId());
        }
        if (StringUtils.isNotEmpty(request.getEmail())) {
            return userDao.getUserByEmail(request.getEmail());
        }
        return null;
    }

    @Override
    public User getDummyUser(String dummyUid) {
        return userDao.getDummyUserById(dummyUid);
    }

    @Override
    public int addUser(User user) {
        return userDao.insert(user);
    }

    @Override
    public int addDummyUser(String userId) {
        return userDao.insertForDummy(userId);
    }


    @Override
    public int updateUserPreferInfo(String userId, String preferInfo) {
        return userDao.updateUserPreferInfo(userId, preferInfo);
    }

    @Override
    public int modifyUserPwd(String userId, String encryptPwd) {
        return userDao.updateUserPassword(userId, encryptPwd);
    }

    @Override
    public int resetUserPwd(String email, String salt, String encryptPwd) {
        return userDao.resetUserPassword(email, salt, encryptPwd);
    }

    @Override
    public int rebindEmail(String userId, String email) {
        return userDao.updateUserEmail(userId, email);
    }

    @Override
    public int modifyUserName(String userId, String userName) {
        return userDao.updateUserName(userId, userName);
    }
}
