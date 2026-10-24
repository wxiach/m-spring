package com.mspring.aop;

import com.mspring.aop.annotation.AfterReturning;
import com.mspring.aop.annotation.AfterThrowing;
import com.mspring.aop.annotation.Around;
import com.mspring.aop.annotation.Before;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 切点与通知的组合：方法匹配切点后，由哪个切面方法、以何种通知类型执行。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public record Advisor(PointcutExpression pointcut, AdviceType type, Object aspectInstance, Method adviceMethod) {

    public enum AdviceType {
        BEFORE(Before.class, JoinPoint.class),
        AFTER_RETURNING(AfterReturning.class, JoinPoint.class, Object.class),
        AFTER_THROWING(AfterThrowing.class, JoinPoint.class, Throwable.class),
        AROUND(Around.class, ProceedingJoinPoint.class);

        // 对应的通知注解；声明顺序即通知的分组顺序
        private final Class<? extends Annotation> annotation;
        // 固定的通知方法签名
        private final Class<?>[] signature;

        AdviceType(Class<? extends Annotation> annotation, Class<?>... signature) {
            this.annotation = annotation;
            this.signature = signature;
        }

        public Class<? extends Annotation> annotation() {
            return annotation;
        }

        // 通知注解只有 value() 一个属性，即切点表达式
        public String pointcutOf(Method method) {
            try {
                Annotation advice = method.getAnnotation(annotation);
                return (String) annotation.getMethod("value").invoke(advice);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("无法读取通知注解的切点表达式：" + method, e);
            }
        }

        // 通知方法是否符合固定签名：个数一致且参数类型能接收实参
        public boolean accepts(Method method) {
            Class<?>[] params = method.getParameterTypes();
            if (params.length != signature.length) {
                return false;
            }
            for (int i = 0; i < params.length; i++) {
                if (!params[i].isAssignableFrom(signature[i])) {
                    return false;
                }
            }
            return true;
        }
    }
}
