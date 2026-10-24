package com.mspring.processor;

public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

    default void postProcessProperties(Object bean, String beanName) {
    }

    /** 若 Bean 最终要替换为代理，必须在此处提前替换，否则循环依赖方与容器缓存的不是同一实例 */
    default Object getEarlyBeanReference(Object bean, String beanName) {
        return bean;
    }
}
