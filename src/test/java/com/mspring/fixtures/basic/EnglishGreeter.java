package com.mspring.fixtures.basic;

import com.mspring.annotation.Component;

@Component
public class EnglishGreeter implements Greeter {

    @Override
    public String greet() {
        return "hello";
    }
}
