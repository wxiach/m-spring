package com.mspring.aop;

import java.lang.reflect.Method;

/**
 * 环绕通知拿到的调用信息。执行 proceed() 才会继续后续通知和目标方法。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public class ProceedingJoinPoint extends JoinPoint {

    private final MethodInvocation invocation;
    private final int nextIndex;

    ProceedingJoinPoint(Object target, Method method, Object[] arguments,
                        MethodInvocation invocation, int nextIndex) {
        super(target, method, arguments);
        this.invocation = invocation;
        this.nextIndex = nextIndex;
    }

    /**
     * 从当前环绕通知之后继续执行，返回后续通知链的结果。
     */
    public Object proceed() throws Throwable {
        return invocation.proceed(nextIndex);
    }
}
