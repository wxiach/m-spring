package com.mspring.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class DefaultSingletonBeanRegistry {

    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    // 早期引用工厂：仅当循环依赖方来获取时才执行（如生成 AOP 代理），没有循环依赖则永远用不到
    private final Map<String, Supplier<Object>> singletonFactories = new ConcurrentHashMap<>();
    private final Set<String> beansInCreation = ConcurrentHashMap.newKeySet();

    protected Object getSingleton(String beanName) {
        Object bean = singletonObjects.get(beanName);
        if (bean != null) {
            return bean;
        }
        bean = earlySingletonObjects.get(beanName);
        if (bean == null) {
            Supplier<Object> factory = singletonFactories.get(beanName);
            if (factory == null) {
                return null;
            }
            bean = factory.get();
            earlySingletonObjects.put(beanName, bean);
            singletonFactories.remove(beanName);
        }
        return bean;
    }

    protected void addSingletonFactory(String beanName, Supplier<Object> factory) {
        singletonFactories.put(beanName, factory);
    }

    protected void registerSingleton(String beanName, Object bean) {
        singletonObjects.put(beanName, bean);
    }

    // 返回 false 表示该 Bean 已在创建中，即撞上构造器循环依赖
    protected boolean beforeBeanCreation(String beanName) {
        return beansInCreation.add(beanName);
    }

    protected void afterBeanCreation(String beanName) {
        beansInCreation.remove(beanName);
    }

    protected void clearEarlyState(String beanName) {
        earlySingletonObjects.remove(beanName);
        singletonFactories.remove(beanName);
    }

    protected Map<String, Object> getSingletons() {
        return singletonObjects;
    }

    protected void clearSingletons() {
        singletonObjects.clear();
    }
}
