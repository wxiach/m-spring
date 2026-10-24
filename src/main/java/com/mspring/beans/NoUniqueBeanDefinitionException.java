package com.mspring.beans;

/**
 * 按类型查找 Bean 时无法唯一确定结果（候选数为 0 或多个）时抛出。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class NoUniqueBeanDefinitionException extends BeansException {

    public NoUniqueBeanDefinitionException(String message) {
        super(message);
    }

    public NoUniqueBeanDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
