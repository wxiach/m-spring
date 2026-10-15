package com.mspring.fixtures.circular.constructor;

import com.mspring.annotation.Component;

@Component
public class LoopA {

    private final LoopB loopB;

    public LoopA(LoopB loopB) {
        this.loopB = loopB;
    }
}
