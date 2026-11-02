package com.mspring.beans;

/**
 * 容器的通用异常，使用消息说明失败发生在哪一步。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public class BeansException extends RuntimeException {

    public BeansException(String message) {
        super(message);
    }

    public BeansException(String message, Throwable cause) {
        super(message, cause);
    }
}
