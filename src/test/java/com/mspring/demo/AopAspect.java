package com.mspring.demo;

import com.mspring.aop.*;
import com.mspring.context.annotation.Component;

/**
 * 记录 AopTarget 的调用顺序，覆盖四种通知。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
@Component
@Aspect
public class AopAspect {

    private static final String POINTCUT = "execution(* com.mspring.demo.AopTarget.call(..))";
    public String str = "";

    @Before(POINTCUT)
    public void before(JoinPoint joinPoint) {
        str += "b";
    }

    @Around(POINTCUT)
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        str += "i";
        Object result = joinPoint.proceed();
        str += "o";
        return result;
    }

    @AfterReturning(POINTCUT)
    public void afterReturning(JoinPoint joinPoint, Object result) {
        str += "r";
    }

    @AfterThrowing(POINTCUT)
    public void afterThrowing(JoinPoint joinPoint, Throwable throwable) {
        str += "t";
    }
}
