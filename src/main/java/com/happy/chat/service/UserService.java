package com.happy.chat.service;

import com.happy.chat.model.User;
import com.happy.chat.model.UserGetRequest;

public interface UserService {
    User getUser(UserGetRequest userGetRequest);
}
