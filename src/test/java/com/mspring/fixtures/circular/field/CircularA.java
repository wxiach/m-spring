package com.mspring.fixtures.circular.field;

import com.mspring.annotation.Autowired;
import com.mspring.annotation.Component;

@Component
public class CircularA {

    @Autowired
    private CircularB b;

    public CircularB getB() {
        return b;
    }
}
