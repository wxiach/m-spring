package com.mspring.beans.config;

/**
 * BeanDefinition 注册表：只暴露注册能力的窄接口。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public interface BeanDefinitionRegistry {

    void registerBeanDefinition(BeanDefinition beanDefinition);
}
