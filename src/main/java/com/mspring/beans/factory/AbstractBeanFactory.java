package com.mspring.beans.factory;

import com.mspring.beans.BeanCreationException;
import com.mspring.beans.BeanCurrentlyInCreationException;
import com.mspring.beans.config.BeanDefinition;
import com.mspring.beans.config.BeanPostProcessor;
import com.mspring.beans.config.DestructionAwareBeanPostProcessor;
import com.mspring.beans.config.InstantiationAwareBeanPostProcessor;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bean 创建流程的抽象基类：实例化 → 属性填充 → 初始化，由已注册的 BeanPostProcessor 驱动。
 * BeanDefinition 的存储与 Bean 的获取由子类实现。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public abstract class AbstractBeanFactory extends SingletonBeanRegistry implements BeanFactory {

    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();

    // ---------------------------------------------------------------------
    // BeanPostProcessor 管理
    // ---------------------------------------------------------------------

    public void addBeanPostProcessor(BeanPostProcessor processor) {
        beanPostProcessors.add(processor);
    }

    protected List<BeanPostProcessor> getBeanPostProcessors() {
        return beanPostProcessors;
    }

    // ---------------------------------------------------------------------
    // Bean 创建流程：实例化 → 属性填充 → 初始化
    // ---------------------------------------------------------------------

    protected Object createBean(BeanDefinition beanDefinition) {
        String beanName = beanDefinition.beanName();
        // 创建前先标记：若 beanName 已在创建中，说明撞上构造器循环依赖（三级缓存救不了，只能报错）
        if (!beforeSingletonCreation(beanName)) {
            throw new BeanCurrentlyInCreationException("检测到循环依赖：" + beanName + " 正在创建中");
        }
        try {
            Object bean = createBeanInstance(beanDefinition);
            // 实例化后立刻暴露早期引用工厂，字段注入的循环依赖方才能拿到半成品引用
            if (beanDefinition.isSingleton()) {
                addSingletonFactory(beanName, () -> getEarlyBeanReference(bean, beanName));
            }
            populateBean(bean, beanName);
            return initializeBean(bean, beanName);
        } finally {
            // 无论成败都要移除标记，否则后续正常获取会被误判为循环依赖
            afterSingletonCreation(beanName);
        }
    }

    // 实例化阶段：类必有默认构造器，故构造器数量大于 1 时才需要报错
    protected Object createBeanInstance(BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.beanClass();
        try {
            Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
            if (constructors.length > 1) {
                throw new BeanCreationException(beanClass.getName() + " 存在多个构造器");
            }
            constructors[0].setAccessible(true);
            Object[] args = Arrays.stream(constructors[0].getParameters())
                    .map(parameter -> getBean(parameter.getType()))
                    .toArray();
            return constructors[0].newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new BeanCreationException("实例化 Bean 失败：" + beanClass.getName(), e);
        }
    }

    protected void populateBean(Object bean, String beanName) {
        for (BeanPostProcessor processor : beanPostProcessors) {
            if (processor instanceof InstantiationAwareBeanPostProcessor aware) {
                aware.postProcessProperties(bean, beanName);
            }
        }
    }

    protected Object initializeBean(Object bean, String beanName) {
        Object current = bean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            current = processor.postProcessBeforeInitialization(current, beanName);
        }
        for (BeanPostProcessor processor : beanPostProcessors) {
            current = processor.postProcessAfterInitialization(current, beanName);
        }
        return current;
    }

    protected Object getEarlyBeanReference(Object bean, String beanName) {
        Object current = bean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            if (processor instanceof InstantiationAwareBeanPostProcessor aware) {
                current = aware.getEarlyBeanReference(current, beanName);
            }
        }
        return current;
    }

    // ---------------------------------------------------------------------
    // Bean 销毁
    // ---------------------------------------------------------------------

    public void destroySingletons() {
        for (Map.Entry<String, Object> entry : getSingletons().entrySet()) {
            for (BeanPostProcessor processor : beanPostProcessors) {
                if (processor instanceof DestructionAwareBeanPostProcessor aware) {
                    aware.postProcessBeforeDestruction(entry.getValue(), entry.getKey());
                }
            }
        }
        clearSingletons();
    }
}
