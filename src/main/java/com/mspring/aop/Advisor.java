package com.mspring.aop;

import java.lang.reflect.Method;

/**
 * 切点与通知的组合：方法匹配切点后，由哪个切面方法、以何种通知类型执行。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public record Advisor(PointcutExpression pointcut, AdviceType type, Object aspectInstance, Method adviceMethod) {

    public enum AdviceType {
        BEFORE,
        AFTER_RETURNING,
        AFTER_THROWING,
        AROUND
    }
}
