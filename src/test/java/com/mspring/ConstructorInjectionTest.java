package com.mspring;

import com.mspring.fixtures.basic.ConstructorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstructorInjectionTest {

    private final ApplicationContext context = new ApplicationContext("com.mspring.fixtures.basic");

    @Test
    void injectsViaSingleConstructor() {
        assertEquals("m-spring", context.getBean(ConstructorService.class).findUserName());
    }
}
