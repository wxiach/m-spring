package com.mspring.fixtures.aop;

import com.mspring.aop.JoinPoint;
import com.mspring.aop.ProceedingJoinPoint;
import com.mspring.annotation.AfterReturning;
import com.mspring.annotation.AfterThrowing;
import com.mspring.annotation.Around;
import com.mspring.annotation.Aspect;
import com.mspring.annotation.Before;
import com.mspring.annotation.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Aspect
public class LogAspect {

    public static final String POINTCUT =
            "execution(* com.mspring.fixtures.aop.OrderService.createOrder|queryOrder|fail(..))";

    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    @Before(POINTCUT)
    public void logBefore(JoinPoint joinPoint) {
        EVENTS.add("before");
    }

    @Around(POINTCUT)
    public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
        EVENTS.add("around-before");
        Object result = joinPoint.proceed();
        EVENTS.add("around-after");
        return result instanceof String text ? text + ":traced" : result;
    }

    @AfterReturning(POINTCUT)
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        EVENTS.add("after-returning");
    }

    @AfterThrowing(POINTCUT)
    public void logAfterThrowing(JoinPoint joinPoint, Throwable throwable) {
        EVENTS.add("after-throwing");
    }
}
