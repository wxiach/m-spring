package com.mspring.aop;

import java.lang.reflect.Method;

/**
 * 可继续执行的连接点：环绕通知中通过 proceed() 调用目标方法。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
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
