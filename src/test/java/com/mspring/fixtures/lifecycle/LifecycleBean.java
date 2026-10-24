package com.mspring.fixtures.lifecycle;

import com.mspring.annotation.Component;
import com.mspring.annotation.PostConstruct;
import com.mspring.annotation.PreDestroy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class LifecycleBean {

    public static final List<String> CALLS = new CopyOnWriteArrayList<>();

    @PostConstruct
    void init() {
        CALLS.add("init");
    }

    @PreDestroy
    void cleanup() {
        CALLS.add("cleanup");
    }
}
