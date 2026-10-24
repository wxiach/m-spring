package com.mspring.beans.config;

/**
 * 感知实例化的扩展点：属性填充，以及循环依赖时提供早期引用。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

    default void postProcessProperties(Object bean, String beanName) {
    }

    /**
     * 若 Bean 最终要替换为代理，必须在此处提前替换，否则循环依赖方与容器缓存的不是同一实例
     */
    default Object getEarlyBeanReference(Object bean, String beanName) {
        return bean;
    }
}
