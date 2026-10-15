package com.mspring.processor;

/** Bean 初始化前后的扩展点；返回值替换容器中的 Bean（如返回代理对象） */
public interface BeanPostProcessor {

    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
