package com.mspring;

import com.mspring.fixtures.basic.PrototypeBean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;

class ScopeTest {

    private final ApplicationContext context = new ApplicationContext("com.mspring.fixtures.basic");

    @Test
    void prototypeCreatesNewInstanceEachTime() {
        assertNotSame(context.getBean(PrototypeBean.class), context.getBean(PrototypeBean.class));
    }
}
