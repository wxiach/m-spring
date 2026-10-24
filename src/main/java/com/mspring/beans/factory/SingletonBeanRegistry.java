package com.mspring.beans.factory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 单例注册中心：通过三级缓存（成品单例 → 早期引用 → 早期引用工厂）
 * 解决字段注入场景下的循环依赖。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class SingletonBeanRegistry {

    // 一级缓存：创建完成的成品单例
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    // 二级缓存：从工厂提前取出的早期引用，避免循环依赖方重复触发工厂
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    // 三级缓存：早期引用工厂，仅当循环依赖方来获取时才执行（如生成 AOP 代理），没有循环依赖则永远用不到
    private final Map<String, Supplier<Object>> singletonFactories = new ConcurrentHashMap<>();
    // 创建中的 Bean 集合：add 返回 false 即撞上构造器循环依赖
    private final Set<String> singletonsCurrentlyInCreation = ConcurrentHashMap.newKeySet();

    // ---------------------------------------------------------------------
    // 三级缓存访问
    // ---------------------------------------------------------------------

    protected Object getSingleton(String beanName, Supplier<Object> singletonFactory) {
        // 一级：成品单例
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject != null) {
            return singletonObject;
        }
        // 二级：已取出的早期引用
        singletonObject = earlySingletonObjects.get(beanName);
        if (singletonObject != null) {
            return singletonObject;
        }
        // 三级：执行早期引用工厂，结果提升到二级缓存
        Supplier<Object> earlyFactory = singletonFactories.get(beanName);
        if (earlyFactory != null) {
            singletonObject = earlyFactory.get();
            earlySingletonObjects.put(beanName, singletonObject);
            singletonFactories.remove(beanName);
            return singletonObject;
        }
        // 三级缓存全部未命中：完整创建并注册为单例，finally 清掉创建期间残留的早期状态
        try {
            singletonObject = singletonFactory.get();
            registerSingleton(beanName, singletonObject);
            return singletonObject;
        } finally {
            clearEarlyState(beanName);
        }
    }

    // ---------------------------------------------------------------------
    // 单例注册
    // ---------------------------------------------------------------------

    protected void addSingletonFactory(String beanName, Supplier<Object> factory) {
        singletonFactories.put(beanName, factory);
    }

    protected void registerSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
    }

    // ---------------------------------------------------------------------
    // 创建状态标记
    // ---------------------------------------------------------------------

    // 返回 false 表示该 Bean 已在创建中，即撞上构造器循环依赖
    protected boolean beforeSingletonCreation(String beanName) {
        return singletonsCurrentlyInCreation.add(beanName);
    }

    protected void afterSingletonCreation(String beanName) {
        singletonsCurrentlyInCreation.remove(beanName);
    }

    // ---------------------------------------------------------------------
    // 清理
    // ---------------------------------------------------------------------

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
