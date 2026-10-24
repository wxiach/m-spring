package com.mspring.beans;

/**
 * Bean 创建过程中（实例化、依赖注入、初始化）发生失败时抛出。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class BeanCreationException extends BeansException {

    public BeanCreationException(String message) {
        super(message);
    }

    public BeanCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
