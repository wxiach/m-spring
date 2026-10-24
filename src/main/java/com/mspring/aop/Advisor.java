package com.mspring.aop;

import java.lang.reflect.Method;

public record Advisor(PointcutExpression pointcut, AdviceType type, Object aspectInstance, Method adviceMethod) {

    public enum AdviceType {
        BEFORE,
        AFTER_RETURNING,
        AFTER_THROWING,
        AROUND
    }
}
