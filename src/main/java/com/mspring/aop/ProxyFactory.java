package com.mspring.aop;

import com.mspring.beans.BeansException;
import com.mspring.beans.factory.BeanFactory;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isBridge;
import static net.bytebuddy.matcher.ElementMatchers.isVirtual;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * 只负责生成代理。通知如何执行，交给 MethodInvocation。
 * 代理继承目标类，所以不能代理 final 类、final 方法或 private 方法。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public final class ProxyFactory {

    // 代理对象不应再次执行业务构造器，避免重复产生副作用。
    private static final Objenesis OBJENESIS = new ObjenesisStd();

    private ProxyFactory() {
    }

    /**
     * 生成目标类的子类代理，将方法调用交给通知链；代理实例不重复执行构造器。
     */
    public static Object createProxy(Object target, List<Advisor> advisors, BeanFactory beanFactory) {
        try {
            // 拦截可重写的实例方法，排除编译器为泛型等场景生成的桥接方法。
            Class<?> proxyClass = new ByteBuddy()
                    .subclass(target.getClass())
                    .method(isVirtual().and(not(isBridge())))
                    .intercept(MethodDelegation.to(new AopInterceptor(target, advisors, beanFactory)))
                    .make()
                    .load(target.getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();

            return OBJENESIS.newInstance(proxyClass);
        } catch (Exception exception) {
            throw new BeansException("创建代理失败：" + target.getClass().getName(), exception);
        }
    }

    /**
     * Byte Buddy 把代理方法转发到这里；每次调用都创建独立的通知链。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    public static class AopInterceptor {

        private final Object target;
        private final List<Advisor> advisors;
        private final BeanFactory beanFactory;

        public AopInterceptor(Object target, List<Advisor> advisors, BeanFactory beanFactory) {
            this.target = target;
            this.advisors = advisors;
            this.beanFactory = beanFactory;
        }

        /**
         * 创建代理时只筛选目标类，这里再筛选当前方法并创建独立的调用链。
         * 通知链最终调用保存的目标对象，避免再次进入代理。
         */
        @RuntimeType
        public Object intercept(@Origin Method method, @AllArguments Object[] arguments) throws Throwable {
            List<Advisor> matched = new ArrayList<>();
            for (Advisor advisor : advisors) {
                if (advisor.getPointcut().matches(target.getClass(), method)) {
                    matched.add(advisor);
                }
            }

            MethodInvocation invocation = new MethodInvocation(target, method, arguments, matched, beanFactory);
            return invocation.proceed();
        }
    }
}
