package com.mspring.aop;

import java.lang.reflect.Method;

public class ProceedingJoinPoint extends JoinPoint {

    private final Invocation invocation;

    public ProceedingJoinPoint(Object target, Method method, Object[] args, Invocation invocation) {
        super(target, method, args);
        this.invocation = invocation;
    }

    public Object proceed() throws Throwable {
        return invocation.proceed();
    }

    @FunctionalInterface
    public interface Invocation {

        Object proceed() throws Throwable;
    }
}
