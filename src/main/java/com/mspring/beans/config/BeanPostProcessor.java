package com.mspring.beans.config;

/**
 * Bean 初始化前后的扩展点；返回值替换容器中的 Bean（如返回代理对象）。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public interface BeanPostProcessor {

    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
