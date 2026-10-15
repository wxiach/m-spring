package com.mspring.aop;

import java.lang.reflect.Method;

public class JoinPoint {

    private final Object target;
    private final Method method;
    private final Object[] args;

    public JoinPoint(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }
}
