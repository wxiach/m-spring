package com.mspring;

import com.mspring.fixtures.basic.GreeterClient;
import com.mspring.fixtures.basic.UserService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationContextTest {

    private final ApplicationContext context = new ApplicationContext("com.mspring.fixtures.basic");

    @Test
    void getBeanReturnsScannedComponent() {
        assertNotNull(context.getBean(UserService.class));
    }

    @Test
    void autowiredFieldIsInjected() {
        assertEquals("m-spring", context.getBean(UserService.class).findUserName());
    }

    @Test
    void injectsByInterfaceType() {
        assertEquals("hello", context.getBean(GreeterClient.class).greet());
    }

    @Test
    void singletonByDefault() {
        assertSame(context.getBean(UserService.class), context.getBean(UserService.class));
    }

    @Test
    void getBeanByName() {
        assertSame(context.getBean(UserService.class), context.getBean("userService"));
    }

    @Test
    void unknownBeanThrows() {
        assertThrows(RuntimeException.class, () -> context.getBean(String.class));
    }
}
