package com.mspring.fixtures.eager;

import com.mspring.annotation.Component;
import com.mspring.annotation.Scope;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope("prototype")
public class PrototypeProbe {

    public static final AtomicInteger COUNT = new AtomicInteger();

    public PrototypeProbe() {
        COUNT.incrementAndGet();
    }
}
