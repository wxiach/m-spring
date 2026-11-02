package com.mspring.aop;

import com.mspring.aop.annotation.After;
import com.mspring.aop.annotation.AfterReturning;
import com.mspring.aop.annotation.AfterThrowing;
import com.mspring.aop.annotation.Around;
import com.mspring.aop.annotation.Before;
import com.mspring.beans.BeansException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 一条通知的信息：切点、通知类型、切面名称和通知方法。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public final class Advisor {

    private final PointcutExpression pointcut;
    private final AdviceType type;
    private final String aspectBeanName;
    private final Method adviceMethod;

    public Advisor(PointcutExpression pointcut, AdviceType type, String aspectBeanName, Method adviceMethod) {
        this.pointcut = pointcut;
        this.type = type;
        this.aspectBeanName = aspectBeanName;
        this.adviceMethod = adviceMethod;
    }

    public PointcutExpression getPointcut() {
        return pointcut;
    }

    public AdviceType getType() {
        return type;
    }

    public String getAspectBeanName() {
        return aspectBeanName;
    }

    public Method getAdviceMethod() {
        return adviceMethod;
    }

    /**
     * 五种通知类型及其对应注解。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    public enum AdviceType {

        BEFORE(Before.class),
        AFTER(After.class),
        AFTER_RETURNING(AfterReturning.class),
        AFTER_THROWING(AfterThrowing.class),
        AROUND(Around.class);

        private final Class<? extends Annotation> annotationType;

        AdviceType(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        public Class<? extends Annotation> getAnnotationType() {
            return annotationType;
        }

        /**
         * 按当前通知类型，读取方法上对应注解的切点表达式。
         */
        public String getExpression(Method method) {
            switch (this) {
                case BEFORE:
                    return method.getAnnotation(Before.class).value();
                case AFTER:
                    return method.getAnnotation(After.class).value();
                case AFTER_RETURNING:
                    return method.getAnnotation(AfterReturning.class).value();
                case AFTER_THROWING:
                    return method.getAnnotation(AfterThrowing.class).value();
                case AROUND:
                    return method.getAnnotation(Around.class).value();
                default:
                    throw new BeansException("未知通知类型：" + this);
            }
        }
    }
}
