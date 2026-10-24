package com.mspring.context.annotation;

import com.mspring.beans.BeanCreationException;
import com.mspring.beans.BeansException;
import com.mspring.beans.config.DestructionAwareBeanPostProcessor;
import com.mspring.core.AnnotationUtils;
import com.mspring.core.Ordered;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * 处理 @PostConstruct / @PreDestroy 生命周期回调：
 * 初始化前执行 @PostConstruct 方法，销毁前执行 @PreDestroy 方法。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class CommonAnnotationProcessor implements DestructionAwareBeanPostProcessor, Ordered {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        invokeAnnotatedMethods(bean, beanName, PostConstruct.class, BeanCreationException::new);
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) {
        invokeAnnotatedMethods(bean, beanName, PreDestroy.class, BeansException::new);
    }

    private void invokeAnnotatedMethods(Object bean, String beanName, Class<? extends Annotation> annotationType,
                                        BiFunction<String, Throwable, BeansException> exceptionFactory) {
        for (Method method : AnnotationUtils.findMethodsAnnotatedWith(bean.getClass(), annotationType)) {
            try {
                method.setAccessible(true);
                method.invoke(bean);
            } catch (ReflectiveOperationException e) {
                throw exceptionFactory.apply(
                        "执行 @" + annotationType.getSimpleName() + " 方法失败：" + beanName + "#" + method.getName(), e);
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
