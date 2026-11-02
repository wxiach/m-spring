package com.mspring.beans.config;

/**
 * Bean 创建过程中的处理步骤，按注册顺序调用。
 * 内置处理器负责字段注入和 AOP；不需要的步骤使用默认实现。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public interface BeanPostProcessor {

    /**
     * 实例化之后填充字段。
     */
    default void postProcessProperties(Object bean, String beanName) {
    }

    /**
     * 循环依赖需要提前引用时调用，AOP 在这里提供代理。
     */
    default Object getEarlyBeanReference(Object bean, String beanName) {
        return bean;
    }

    /**
     * 在初始化前处理 Bean，默认返回原对象。
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * 在初始化后处理 Bean，可返回包装后的对象，例如 AOP 代理。
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
