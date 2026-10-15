package com.mspring.aop;

import com.mspring.aop.Advisor.AdviceType;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ProxyFactory {

    private static Constructor<?> chooseConstructor(Class<?> proxyClass) {
        Constructor<?>[] constructors = proxyClass.getDeclaredConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }
        try {
            return proxyClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("代理类没有可用的构造器：" + proxyClass.getName(), e);
        }
    }

    // 代理实例的字段不参与调用，构造器参数以默认值占位即可
    private static Object[] defaultArguments(Class<?>[] parameterTypes) {
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            args[i] = defaultValue(parameterTypes[i]);
        }
        return args;
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        return 0D;
    }

    public Object createProxy(Object target, Class<?> targetClass, List<Advisor> advisors) {
        if (Modifier.isFinal(targetClass.getModifiers())) {
            throw new IllegalStateException("final 类无法生成子类代理：" + targetClass.getName());
        }
        Class<?> proxyClass = new ByteBuddy()
                .subclass(targetClass)
                .method(ElementMatchers.isVirtual()
                        .and(ElementMatchers.not(ElementMatchers.isFinal()))
                        .and(ElementMatchers.not(ElementMatchers.isBridge())))
                .intercept(MethodDelegation.to(new AopInterceptor(target, advisors)))
                .make()
                .load(targetClass.getClassLoader())
                .getLoaded();
        try {
            Constructor<?> constructor = chooseConstructor(proxyClass);
            constructor.setAccessible(true);
            return constructor.newInstance(defaultArguments(constructor.getParameterTypes()));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("实例化代理失败：" + proxyClass.getName(), e);
        }
    }

    public static class AopInterceptor {

        private final Object target;
        private final List<Advisor> advisors;

        public AopInterceptor(Object target, List<Advisor> advisors) {
            this.target = target;
            this.advisors = advisors;
        }

        private static void requireJoinPointParameter(Method advice) {
            if (!advice.getParameterTypes()[0].isAssignableFrom(JoinPoint.class)) {
                throw new IllegalStateException("通知方法的第一个参数必须是 JoinPoint：" + advice);
            }
        }

        @RuntimeType
        public Object intercept(@Origin Method method, @AllArguments Object[] args) throws Throwable {
            List<Advisor> matched = new ArrayList<>();
            for (Advisor advisor : advisors) {
                if (advisor.pointcut().matches(method)) {
                    matched.add(advisor);
                }
            }
            if (matched.isEmpty()) {
                return invokeTarget(method, args);
            }
            JoinPoint joinPoint = new JoinPoint(target, method, args);
            for (Advisor advisor : matched) {
                if (advisor.type() == AdviceType.BEFORE) {
                    invokeAdvice(advisor, joinPoint, null);
                }
            }
            Object result;
            try {
                result = proceed(matched, method, args);
            } catch (Throwable throwable) {
                for (Advisor advisor : matched) {
                    if (advisor.type() == AdviceType.AFTER_THROWING) {
                        invokeAdvice(advisor, joinPoint, throwable);
                    }
                }
                throw throwable;
            }
            for (Advisor advisor : matched) {
                if (advisor.type() == AdviceType.AFTER_RETURNING) {
                    invokeAdvice(advisor, joinPoint, result);
                }
            }
            return result;
        }

        private Object proceed(List<Advisor> matched, Method method, Object[] args) throws Throwable {
            ProceedingJoinPoint.Invocation invocation = () -> invokeTarget(method, args);
            for (int i = matched.size() - 1; i >= 0; i--) {
                Advisor advisor = matched.get(i);
                if (advisor.type() != AdviceType.AROUND) {
                    continue;
                }
                ProceedingJoinPoint.Invocation next = invocation;
                invocation = () -> invokeAround(advisor, new ProceedingJoinPoint(target, method, args, next));
            }
            return invocation.proceed();
        }

        private Object invokeTarget(Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        /**
         * 通知方法签名为零参、(JoinPoint) 或 (JoinPoint, 上下文)，上下文即返回值（@AfterReturning）或异常（@AfterThrowing）
         */
        private void invokeAdvice(Advisor advisor, JoinPoint joinPoint, Object context) throws Throwable {
            Method advice = advisor.adviceMethod();
            switch (advice.getParameterCount()) {
                case 0 -> invoke(advisor);
                case 1 -> {
                    requireJoinPointParameter(advice);
                    invoke(advisor, joinPoint);
                }
                case 2 -> {
                    requireJoinPointParameter(advice);
                    invoke(advisor, joinPoint, context);
                }
                default -> throw new IllegalStateException(
                        "无法识别的通知方法签名（支持零参、JoinPoint 单参或 JoinPoint + 上下文两参）：" + advice);
            }
        }

        private Object invokeAround(Advisor advisor, ProceedingJoinPoint joinPoint) throws Throwable {
            Method advice = advisor.adviceMethod();
            if (advice.getParameterCount() != 1
                    || !advice.getParameterTypes()[0].isAssignableFrom(ProceedingJoinPoint.class)) {
                throw new IllegalStateException("@Around 通知方法必须声明一个 ProceedingJoinPoint 参数：" + advice);
            }
            return invoke(advisor, joinPoint);
        }

        private Object invoke(Advisor advisor, Object... args) throws Throwable {
            try {
                Method advice = advisor.adviceMethod();
                advice.setAccessible(true);
                return advice.invoke(advisor.aspectInstance(), args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
