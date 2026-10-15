package com.mspring.fixtures.basic;

import com.mspring.annotation.Autowired;
import com.mspring.annotation.Component;

@Component
public class UserService {

    @Autowired
    private UserDao userDao;

    public String findUserName() {
        return userDao.findName();
    }
}
