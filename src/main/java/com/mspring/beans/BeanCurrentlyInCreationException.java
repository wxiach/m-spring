package com.mspring.beans;

/**
 * Bean 正在创建中时又被请求创建，即检测到无法解决的构造器循环依赖时抛出。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class BeanCurrentlyInCreationException extends BeansException {

    public BeanCurrentlyInCreationException(String message) {
        super(message);
    }

    public BeanCurrentlyInCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
