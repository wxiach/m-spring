package com.mspring.beans.config;

/**
 * 感知销毁的扩展点：单例销毁前回调。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

    default void postProcessBeforeDestruction(Object bean, String beanName) {
    }
}
