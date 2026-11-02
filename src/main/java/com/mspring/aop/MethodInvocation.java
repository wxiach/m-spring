package com.mspring.aop;

import com.mspring.beans.BeansException;
import com.mspring.beans.factory.BeanFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 保存一次方法调用的信息，并按顺序执行匹配的通知。
 * proceed(index) 从指定位置继续执行，到达通知列表末尾时调用目标方法。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
final class MethodInvocation {

    private final Object target;
    private final Method method;
    private final Object[] arguments;
    private final List<Advisor> advisors;
    private final BeanFactory beanFactory;

    MethodInvocation(Object target, Method method, Object[] arguments,
                     List<Advisor> advisors, BeanFactory beanFactory) {
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        this.advisors = advisors;
        this.beanFactory = beanFactory;
    }

    // ========================================================================
    // 通知链执行
    // ========================================================================

    /**
     * 从第一条通知开始执行本次调用。
     */
    Object proceed() throws Throwable {
        return proceed(0);
    }

    /**
     * 从指定位置继续通知链；到达列表末尾时调用目标方法。
     * 每次调用都使用传入的位置，环绕通知可多次执行后续链路。
     */
    Object proceed(int index) throws Throwable {
        if (index == advisors.size()) {
            return invokeMethod(target, method, arguments);
        }

        Advisor advisor = advisors.get(index);
        JoinPoint joinPoint = new JoinPoint(target, method, arguments);

        switch (advisor.getType()) {
            case BEFORE:
                invokeAdvice(advisor, joinPoint);
                return proceed(index + 1);

            case AFTER:
                // 后续通知或目标方法抛出异常时，也要执行最终通知。
                try {
                    return proceed(index + 1);
                } finally {
                    invokeAdvice(advisor, joinPoint);
                }

            case AFTER_RETURNING:
                Object result = proceed(index + 1);
                invokeAdvice(advisor, joinPoint, result);
                return result;

            case AFTER_THROWING:
                try {
                    return proceed(index + 1);
                } catch (Throwable throwable) {
                    invokeAdvice(advisor, joinPoint, throwable);
                    // 通知执行后继续抛出原异常，让外层通知和调用方感知失败。
                    throw throwable;
                }

            case AROUND:
                // 保存下一条通知的位置，由环绕通知决定是否调用 proceed() 继续。
                ProceedingJoinPoint proceeding = new ProceedingJoinPoint(
                        target, method, arguments, this, index + 1);
                return invokeAdvice(advisor, proceeding);

            default:
                throw new BeansException("未知通知类型：" + advisor.getType());
        }
    }

    // ========================================================================
    // 通知与目标方法的反射调用
    // ========================================================================

    private Object invokeAdvice(Advisor advisor, Object... adviceArguments) throws Throwable {
        Object aspect = beanFactory.getBean(advisor.getAspectBeanName());
        return invokeMethod(aspect, advisor.getAdviceMethod(), adviceArguments);
    }

    /**
     * 反射调用通知或目标方法，并将业务方法抛出的原始异常传回通知链。
     */
    private Object invokeMethod(Object instance, Method invokedMethod, Object[] methodArguments) throws Throwable {
        try {
            invokedMethod.setAccessible(true);
            return invokedMethod.invoke(instance, methodArguments);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        } catch (Exception exception) {
            throw new BeansException("调用方法失败：" + invokedMethod.getName(), exception);
        }
    }
}
