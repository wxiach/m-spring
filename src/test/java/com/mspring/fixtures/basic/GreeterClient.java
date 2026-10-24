package com.mspring.fixtures.basic;

import com.mspring.annotation.Autowired;
import com.mspring.annotation.Component;

@Component
public class GreeterClient {

    @Autowired
    private Greeter greeter;

    public String greet() {
        return greeter.greet();
    }
}
