package com.mspring.processor;

public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

    default void postProcessBeforeDestruction(Object bean, String beanName) {
    }
}
