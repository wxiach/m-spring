package com.mspring.processor;

import com.mspring.annotation.PostConstruct;
import com.mspring.annotation.PreDestroy;
import com.mspring.exception.BeansException;
import com.mspring.core.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class CommonAnnotationProcessor implements DestructionAwareBeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        invokeAnnotatedMethods(bean, beanName, PostConstruct.class);
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) {
        invokeAnnotatedMethods(bean, beanName, PreDestroy.class);
    }

    private void invokeAnnotatedMethods(Object bean, String beanName, Class<? extends Annotation> annotationType) {
        for (Method method : AnnotationUtils.findMethodsAnnotatedWith(bean.getClass(), annotationType)) {
            try {
                method.setAccessible(true);
                method.invoke(bean);
            } catch (ReflectiveOperationException e) {
                throw new BeansException(
                        "执行 @" + annotationType.getSimpleName() + " 方法失败：" + beanName + "#" + method.getName(), e);
            }
        }
    }
}
