package com.mspring.fixtures.circular.constructor;

import com.mspring.annotation.Component;

@Component
public class LoopB {

    private final LoopA loopA;

    public LoopB(LoopA loopA) {
        this.loopA = loopA;
    }
}
