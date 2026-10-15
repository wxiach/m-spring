package com.mspring.processor;

import com.mspring.annotation.Autowired;
import com.mspring.core.BeanFactory;
import com.mspring.exception.BeansException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class AutowiredAnnotationProcessor implements InstantiationAwareBeanPostProcessor {

    private final BeanFactory beanFactory;

    public AutowiredAnnotationProcessor(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessProperties(Object bean, String beanName) {
        // 父类字段同样需要注入，沿继承链向上遍历
        Class<?> current = bean.getClass();
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                injectField(bean, field);
            }
            current = current.getSuperclass();
        }
    }

    private void injectField(Object bean, Field field) {
        if (!field.isAnnotationPresent(Autowired.class)
                || Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
            return;
        }
        try {
            field.setAccessible(true);
            field.set(bean, beanFactory.resolveDependency(field.getType()));
        } catch (IllegalAccessException e) {
            throw new BeansException("注入依赖失败：" + field.getDeclaringClass().getName() + "#" + field.getName(), e);
        }
    }
}
