package com.mspring.fixtures.basic;

import com.mspring.annotation.Component;

@Component
public class UserDao {

    public String findName() {
        return "m-spring";
    }
}
