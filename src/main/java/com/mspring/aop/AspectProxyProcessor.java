package com.mspring.aop;

import com.mspring.aop.Advisor.AdviceType;
import com.mspring.beans.config.BeanDefinition;
import com.mspring.beans.config.InstantiationAwareBeanPostProcessor;
import com.mspring.beans.factory.BeanFactory;
import com.mspring.core.AnnotationUtils;
import com.mspring.core.Ordered;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * AOP 代理处理器：为匹配切点的 Bean 创建代理；循环依赖场景下通过早期引用提前生成代理。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class AspectProxyProcessor implements InstantiationAwareBeanPostProcessor, Ordered {

    private final BeanFactory beanFactory;
    private final ProxyFactory proxyFactory = new ProxyFactory();
    private final Map<String, Object> proxyCache = new ConcurrentHashMap<>();
    private List<Advisor> advisors;
    // 通知列表构建中创建的 Bean（切面及其依赖）不做代理判断，避免递归触发自身
    private boolean initializing;

    public AspectProxyProcessor(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    // ---------------------------------------------------------------------
    // BeanPostProcessor 回调
    // ---------------------------------------------------------------------

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return wrapIfNecessary(bean, beanName);
    }

    @Override
    public Object getEarlyBeanReference(Object bean, String beanName) {
        return wrapIfNecessary(bean, beanName);
    }

    // ---------------------------------------------------------------------
    // 代理包装
    // ---------------------------------------------------------------------

    private Object wrapIfNecessary(Object bean, String beanName) {
        Object cached = proxyCache.get(beanName);
        if (cached != null) {
            return cached;
        }
        Class<?> beanClass = bean.getClass();
        if (beanClass.isAnnotationPresent(Aspect.class)) {
            return bean;
        }
        List<Advisor> matched = new ArrayList<>();
        for (Advisor advisor : advisors()) {
            if (advisor.pointcut().matches(beanClass)) {
                matched.add(advisor);
            }
        }
        if (matched.isEmpty()) {
            return bean;
        }
        Object proxy = proxyFactory.createProxy(bean, matched);
        proxyCache.put(beanName, proxy);
        return proxy;
    }

    // ---------------------------------------------------------------------
    // 通知器（Advisor）构建
    // ---------------------------------------------------------------------

    private List<Advisor> advisors() {
        if (advisors == null) {
            if (initializing) {
                return List.of();
            }
            initializing = true;
            try {
                advisors = buildAdvisors();
            } finally {
                initializing = false;
            }
        }
        return advisors;
    }

    private List<Advisor> buildAdvisors() {
        List<Advisor> result = new ArrayList<>();
        for (BeanDefinition definition : beanFactory.getBeanDefinitions()) {
            Class<?> aspectClass = definition.beanClass();
            if (!aspectClass.isAnnotationPresent(Aspect.class)) {
                continue;
            }
            Object aspect = beanFactory.getBean(definition.beanName());
            collect(result, aspectClass, aspect, Before.class, AdviceType.BEFORE, Before::value);
            collect(result, aspectClass, aspect, AfterReturning.class, AdviceType.AFTER_RETURNING, AfterReturning::value);
            collect(result, aspectClass, aspect, AfterThrowing.class, AdviceType.AFTER_THROWING, AfterThrowing::value);
            collect(result, aspectClass, aspect, Around.class, AdviceType.AROUND, Around::value);
        }
        return result;
    }

    private <A extends Annotation> void collect(List<Advisor> result, Class<?> aspectClass, Object aspect,
                                                Class<A> annotationType, AdviceType type, Function<A, String> pointcut) {
        for (Method method : AnnotationUtils.findMethodsAnnotatedWith(aspectClass, annotationType)) {
            result.add(new Advisor(new PointcutExpression(pointcut.apply(method.getAnnotation(annotationType))),
                    type, aspect, method));
        }
    }
}
