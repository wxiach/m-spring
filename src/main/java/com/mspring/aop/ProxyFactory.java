package com.mspring.aop;

import com.mspring.aop.ProceedingJoinPoint.Invocation;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * 基于 ByteBuddy 创建代理：拦截匹配切点的方法，按通知类型织入切面逻辑。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class ProxyFactory {

    // 绕过构造期直接创建代理对象
    private static final Objenesis OBJENESIS = new ObjenesisStd();

    public static Object createProxy(Object target, List<Advisor> advisors) {
        Class<?> proxyClass = new ByteBuddy()
                .subclass(target.getClass())
                .method(isVirtual().and(not(isBridge())))
                .intercept(MethodDelegation.to(new AopInterceptor(target, advisors)))
                .make()
                .load(target.getClass().getClassLoader())
                .getLoaded();
        return OBJENESIS.newInstance(proxyClass);
    }

    // 织入代理类每个虚方法的拦截器：一次调用 = 建一条通知链走到底
    public static class AopInterceptor {

        private final Object target;
        private final List<Advisor> advisors;

        public AopInterceptor(Object target, List<Advisor> advisors) {
            this.target = target;
            this.advisors = advisors;
        }

        // ---------------------------------------------------------------------
        // 拦截入口
        // ---------------------------------------------------------------------

        @RuntimeType
        public Object intercept(@Origin Method method, @AllArguments Object[] args) throws Throwable {
            List<Advisor> matched = advisors.stream().filter(advisor -> advisor.pointcut().matches(method)).toList();
            JoinPoint point = new JoinPoint(target, method, args);
            // 从尾到头包成调用链：声明在前的通知在最外层，链尾是目标方法
            Invocation invocation = () -> invokeTarget(method, args);
            for (int i = matched.size() - 1; i >= 0; i--) {
                Advisor advisor = matched.get(i);
                Invocation next = invocation;
                invocation = switch (advisor.type()) {
                    case BEFORE -> before(advisor, next, point);
                    case AROUND -> around(advisor, next, point);
                    case AFTER_RETURNING -> afterReturning(advisor, next, point);
                    case AFTER_THROWING -> afterThrowing(advisor, next, point);
                };
            }
            return invocation.proceed();
        }

        // ---------------------------------------------------------------------
        // 通知节点：把一种通知包成链上的一环，next 是链的剩余部分
        // ---------------------------------------------------------------------

        // 前置通知：先执行通知，再继续链
        private Invocation before(Advisor advisor, Invocation next, JoinPoint point) {
            return () -> {
                invokeAdvice(advisor, point);
                return next.proceed();
            };
        }

        // 环绕通知：链的剩余部分交给通知，由它决定何时 proceed
        private Invocation around(Advisor advisor, Invocation next, JoinPoint point) {
            return () -> invokeAdvice(advisor,
                    new ProceedingJoinPoint(point.getTarget(), point.getMethod(), point.getArgs(), next));
        }

        // 返回后通知：先让链走完拿到结果，再执行通知
        private Invocation afterReturning(Advisor advisor, Invocation next, JoinPoint point) {
            return () -> {
                Object result = next.proceed();
                invokeAdvice(advisor, point, result);
                return result;
            };
        }

        // 异常通知：链上抛出异常时先执行通知，再原样抛出
        private Invocation afterThrowing(Advisor advisor, Invocation next, JoinPoint point) {
            return () -> {
                try {
                    return next.proceed();
                } catch (Throwable throwable) {
                    invokeAdvice(advisor, point, throwable);
                    throw throwable;
                }
            };
        }

        // ---------------------------------------------------------------------
        // 目标与通知调用
        // ---------------------------------------------------------------------

        // 反射调用目标方法，还原业务异常
        private Object invokeTarget(Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        // 反射调用通知方法，还原业务异常
        private Object invokeAdvice(Advisor advisor, Object... args) throws Throwable {
            try {
                Method adviceMethod = advisor.adviceMethod();
                adviceMethod.setAccessible(true);
                return adviceMethod.invoke(advisor.aspectInstance(), args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
