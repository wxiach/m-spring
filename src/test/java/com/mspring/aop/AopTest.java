package com.mspring.aop;

import com.mspring.ApplicationContext;
import com.mspring.fixtures.aop.LogAspect;
import com.mspring.fixtures.aop.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AopTest {

    private final ApplicationContext context = new ApplicationContext("com.mspring.fixtures.aop");

    @BeforeEach
    void setUp() {
        LogAspect.EVENTS.clear();
    }

    @Test
    void advicesExecuteInOrder() {
        OrderService service = context.getBean(OrderService.class);
        String result = service.createOrder("book");
        assertEquals("order:book:traced", result);
        assertEquals(List.of("before", "around-before", "around-after", "after-returning"), LogAspect.EVENTS);
    }

    @Test
    void aroundAdviceChangesReturnValue() {
        OrderService service = context.getBean(OrderService.class);
        assertEquals("query:traced", service.queryOrder());
    }

    @Test
    void exceptionTriggersAfterThrowingAndStillPropagates() {
        OrderService service = context.getBean(OrderService.class);
        RuntimeException exception = assertThrows(RuntimeException.class, service::fail);
        assertEquals("boom", exception.getMessage());
        assertEquals(List.of("before", "around-before", "after-throwing"), LogAspect.EVENTS);
    }

    @Test
    void unmatchedMethodTriggersNoAdvice() {
        OrderService service = context.getBean(OrderService.class);
        assertEquals("inventory", service.inventory());
        assertTrue(LogAspect.EVENTS.isEmpty());
    }

    @Test
    void containerHoldsProxyButInjectionStillWorks() {
        OrderService service = context.getBean(OrderService.class);
        assertNotSame(OrderService.class, service.getClass());
        assertTrue(OrderService.class.isAssignableFrom(service.getClass()));
        assertEquals("inventory", service.inventory());
    }
}
