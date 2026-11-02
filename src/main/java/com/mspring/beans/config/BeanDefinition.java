package com.mspring.beans.config;

/**
 * Bean 的定义：名称、类型和作用域。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public final class BeanDefinition {

    public static final String SCOPE_SINGLETON = "singleton";
    public static final String SCOPE_PROTOTYPE = "prototype";

    private final String beanName;
    private final Class<?> beanClass;
    private final String scope;

    public BeanDefinition(String beanName, Class<?> beanClass, String scope) {
        this.beanName = beanName;
        this.beanClass = beanClass;
        this.scope = scope;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public String getScope() {
        return scope;
    }

    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(scope);
    }
}
