package com.happy.chat.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.happy.chat.dao.UserDao;
import com.happy.chat.model.User;
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
        if (StringUtils.isNotEmpty(request.getUserName())) {
            return userDao.getUserByName(request.getUserName());
        }
        return null;
    }
}
