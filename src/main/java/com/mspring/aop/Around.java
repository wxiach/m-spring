package com.mspring.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 环绕通知：包裹目标方法执行，通过 ProceedingJoinPoint 控制目标方法的调用。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Around {

    String value();
}
