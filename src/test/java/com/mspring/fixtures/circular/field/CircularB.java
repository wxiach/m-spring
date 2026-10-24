package com.mspring.fixtures.circular.field;

import com.mspring.annotation.Autowired;
import com.mspring.annotation.Component;

@Component
public class CircularB {

    @Autowired
    private CircularA a;

    public CircularA getA() {
        return a;
    }
}
