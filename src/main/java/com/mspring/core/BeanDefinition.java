package com.mspring.core;

public record BeanDefinition(String beanName, Class<?> beanClass, String scope) {

    boolean isSingleton() {
        return "singleton".equals(scope);
    }
}
