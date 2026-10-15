package com.mspring;

import com.mspring.fixtures.lifecycle.LifecycleBean;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LifecycleTest {

    @Test
    void postConstructRunsAtStartupAndPreDestroyOnClose() {
        LifecycleBean.CALLS.clear();
        ApplicationContext context = new ApplicationContext("com.mspring.fixtures.lifecycle");
        assertEquals(List.of("init"), LifecycleBean.CALLS);
        context.close();
        assertEquals(List.of("init", "cleanup"), LifecycleBean.CALLS);
    }
}
