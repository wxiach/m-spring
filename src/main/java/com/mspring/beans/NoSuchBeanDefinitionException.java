package com.mspring.beans;

/**
 * 容器中不存在指定名称的 Bean 时抛出。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class NoSuchBeanDefinitionException extends BeansException {

    public NoSuchBeanDefinitionException(String message) {
        super(message);
    }

    public NoSuchBeanDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
