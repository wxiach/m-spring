package com.mspring.fixtures.eager;

import com.mspring.annotation.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class EagerProbe {

    public static final AtomicInteger COUNT = new AtomicInteger();

    public EagerProbe() {
        COUNT.incrementAndGet();
    }
}
