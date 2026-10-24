package com.mspring.fixtures.basic;

import com.mspring.annotation.Component;

@Component
public class ConstructorService {

    private final UserDao userDao;

    public ConstructorService(UserDao userDao) {
        this.userDao = userDao;
    }

    public String findUserName() {
        return userDao.findName();
    }
}
