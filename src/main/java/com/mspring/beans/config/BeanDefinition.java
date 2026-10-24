package com.mspring.beans.config;

/**
 * Bean 的定义元数据：名称、类型、作用域。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public record BeanDefinition(String beanName, Class<?> beanClass, String scope) {

    public static final String SCOPE_SINGLETON = "singleton";

    public static final String SCOPE_PROTOTYPE = "prototype";

    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(scope);
    }
}
