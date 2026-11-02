package com.mspring.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 最终通知：无论方法正常返回还是抛出异常，都在结束时执行。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface After {

    String value();
}
