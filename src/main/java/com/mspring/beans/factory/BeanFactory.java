package com.mspring.beans.factory;

import com.mspring.beans.config.BeanDefinition;

import java.util.List;

/**
 * Bean 工厂的只读视图：按名称/类型获取 Bean，并枚举已注册的 BeanDefinition。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public interface BeanFactory {

    Object getBean(String name);

    <T> T getBean(Class<T> type);

    List<BeanDefinition> getBeanDefinitions();
}
