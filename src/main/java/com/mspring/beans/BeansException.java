package com.mspring.beans;

/**
 * 容器异常体系的根类型。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class BeansException extends RuntimeException {

    public BeansException(String message) {
        super(message);
    }

    public BeansException(String message, Throwable cause) {
        super(message, cause);
    }
}
