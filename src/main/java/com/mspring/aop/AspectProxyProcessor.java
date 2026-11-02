package com.mspring.aop;

import com.mspring.aop.Advisor.AdviceType;
import com.mspring.aop.annotation.Aspect;
import com.mspring.beans.config.BeanDefinition;
import com.mspring.beans.config.BeanPostProcessor;
import com.mspring.beans.factory.BeanFactory;
import com.mspring.core.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 把 AOP 接到 IoC 流程上：收集通知，为匹配的 Bean 创建代理。
 * 单例代理交给工厂保存，原型每次创建新代理。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public class AspectProxyProcessor implements BeanPostProcessor {

    private final BeanFactory beanFactory;
    private final List<Advisor> advisors = new ArrayList<>();

    public AspectProxyProcessor(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        collectAdvisors();
    }

    // ========================================================================
    // Bean 创建过程中的代理处理
    // ========================================================================

    /**
     * 循环依赖需要提前引用时，为匹配的 Bean 创建代理。
     */
    @Override
    public Object getEarlyBeanReference(Object bean, String beanName) {
        return wrapIfNecessary(bean);
    }

    /**
     * 优先复用已生成的早期代理，否则在 Bean 创建完成后生成代理。
     * 保证其他 Bean 已注入的引用与容器最终返回的对象一致。
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Object earlyReference = beanFactory.getEarlyBeanReference(beanName);
        if (earlyReference != null) {
            return earlyReference;
        }

        return wrapIfNecessary(bean);
    }

    // ========================================================================
    // 通知收集与代理创建
    // ========================================================================

    /**
     * 收集切面中的通知元信息；切面对象留到实际执行通知时获取。
     */
    private void collectAdvisors() {
        for (BeanDefinition definition : beanFactory.getBeanDefinitions()) {
            if (!definition.getBeanClass().isAnnotationPresent(Aspect.class)) {
                continue;
            }

            // 按通知类型的声明顺序登记；执行时由通知链决定先调用还是后调用。
            for (AdviceType type : AdviceType.values()) {
                for (Method method : AnnotationUtils.findMethodsAnnotatedWith(
                        definition.getBeanClass(), type.getAnnotationType())) {
                    PointcutExpression pointcut = new PointcutExpression(type.getExpression(method));
                    advisors.add(new Advisor(pointcut, type, definition.getBeanName(), method));
                }
            }
        }
    }

    private Object wrapIfNecessary(Object bean) {
        Class<?> beanClass = bean.getClass();
        // 切面和后置处理器参与框架自身的执行流程，不再包装成业务代理。
        if (beanClass.isAnnotationPresent(Aspect.class) || bean instanceof BeanPostProcessor) {
            return bean;
        }

        List<Advisor> matched = new ArrayList<>();
        for (Advisor advisor : advisors) {
            if (advisor.getPointcut().matches(beanClass)) {
                matched.add(advisor);
            }
        }

        if (matched.isEmpty()) {
            return bean;
        }

        return ProxyFactory.createProxy(bean, matched, beanFactory);
    }
}
