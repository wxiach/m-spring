package com.mspring.aop;

import java.lang.reflect.Method;

/**
 * 连接点：一次被拦截的方法调用，携带目标对象、方法与参数。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
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
