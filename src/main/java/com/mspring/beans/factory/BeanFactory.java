package com.mspring.beans.factory;

import com.mspring.beans.BeansException;
import com.mspring.beans.config.BeanDefinition;
import com.mspring.beans.config.BeanPostProcessor;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * IoC 的核心：保存 Bean 定义，完成对象创建、依赖注入和后置处理。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public class BeanFactory {

    private final Map<String, BeanDefinition> beanDefinitions = new LinkedHashMap<>();
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    // 一级缓存：创建完成的单例。
    private final Map<String, Object> singletonObjects = new LinkedHashMap<>();

    // 二级缓存：循环依赖中已经取出的早期引用，可能是 AOP 代理。
    private final Map<String, Object> earlySingletonObjects = new LinkedHashMap<>();

    // 三级缓存：需要早期引用时才调用的工厂，此时 Bean 已经完成构造。
    private final Map<String, EarlyBeanReferenceFactory> singletonFactories = new LinkedHashMap<>();

    // 缓存无法提供引用时，用创建标记阻止同一个 Bean 被递归创建。
    private final Set<String> beansInCreation = new HashSet<>();

    // ========================================================================
    // Bean 定义与后置处理器注册
    // ========================================================================

    /**
     * 登记 Bean 定义，实际对象在获取 Bean 或预创建单例时生成。
     */
    public void registerBeanDefinition(BeanDefinition definition) {
        beanDefinitions.put(definition.getBeanName(), definition);
    }

    /**
     * 注册后置处理器，Bean 创建时按注册顺序调用。
     */
    public void addBeanPostProcessor(BeanPostProcessor processor) {
        beanPostProcessors.add(processor);
    }

    /**
     * 返回已登记的 Bean 定义列表，供组件扫描后的处理器读取。
     */
    public List<BeanDefinition> getBeanDefinitions() {
        return new ArrayList<>(beanDefinitions.values());
    }

    // ========================================================================
    // Bean 获取与单例预创建
    // ========================================================================

    /**
     * 按名称获取 Bean：单例先查缓存，原型每次创建新对象。
     */
    public Object getBean(String name) {
        BeanDefinition definition = beanDefinitions.get(name);
        if (definition == null) {
            throw new BeansException("找不到 Bean：" + name);
        }

        if (definition.isSingleton()) {
            Object singleton = getSingleton(name);
            if (singleton != null) {
                return singleton;
            }
        }

        return createBean(definition);
    }

    /**
     * 按类型获取唯一匹配的 Bean；匹配结果不唯一时抛出 BeansException。
     */
    public <T> T getBean(Class<T> type) {
        List<String> names = new ArrayList<>();
        for (BeanDefinition definition : beanDefinitions.values()) {
            if (type.isAssignableFrom(definition.getBeanClass())) {
                names.add(definition.getBeanName());
            }
        }

        if (names.size() != 1) {
            throw new BeansException("无法确定唯一的 Bean：" + type.getName());
        }

        return type.cast(getBean(names.get(0)));
    }

    /**
     * 读取已经生成的早期引用；只查二级缓存，不触发三级缓存中的工厂。
     * AOP 后置处理器用它复用循环依赖期间生成的代理。
     */
    public Object getEarlyBeanReference(String beanName) {
        return earlySingletonObjects.get(beanName);
    }

    /**
     * 启动时创建所有单例，原型仍在每次获取时创建。
     */
    public void preInstantiateSingletons() {
        for (BeanDefinition definition : beanDefinitions.values()) {
            if (definition.isSingleton()) {
                getBean(definition.getBeanName());
            }
        }
    }

    // ========================================================================
    // Bean 创建与缓存管理
    // ========================================================================

    // 按顺序查三级缓存，工厂生成的引用只需要保存一次。
    private Object getSingleton(String beanName) {
        Object singleton = singletonObjects.get(beanName);
        if (singleton != null) {
            return singleton;
        }

        singleton = earlySingletonObjects.get(beanName);
        if (singleton != null) {
            return singleton;
        }

        EarlyBeanReferenceFactory factory = singletonFactories.get(beanName);
        if (factory != null) {
            // 需要早期引用时才调用工厂；结果移入二级缓存，后续直接复用。
            singleton = factory.getObject();
            earlySingletonObjects.put(beanName, singleton);
            singletonFactories.remove(beanName);
        }

        return singleton;
    }

    /**
     * 创建 Bean：构造对象、登记早期引用工厂、注入字段，再执行后置处理。
     */
    private Object createBean(BeanDefinition definition) {
        String beanName = definition.getBeanName();
        if (!beansInCreation.add(beanName)) {
            throw new BeansException("无法创建存在循环依赖的 Bean：" + beanName);
        }

        try {
            Object bean = createInstance(definition.getBeanClass());
            if (definition.isSingleton()) {
                // 必须先完成构造，再提供早期引用；字段注入时才能解决 A → B → A。
                singletonFactories.put(beanName, new EarlyBeanReferenceFactory(bean, beanName));
            }

            for (BeanPostProcessor processor : beanPostProcessors) {
                processor.postProcessProperties(bean, beanName);
            }

            Object processedBean = applyBeanPostProcessors(bean, beanName);
            if (definition.isSingleton()) {
                singletonObjects.put(beanName, processedBean);
            }

            return processedBean;
        } catch (Exception exception) {
            throw new BeansException("创建 Bean 失败：" + beanName, exception);
        } finally {
            // 无论成功或失败，都清理本次创建的临时状态；完成的单例只留在一级缓存。
            beansInCreation.remove(beanName);
            earlySingletonObjects.remove(beanName);
            singletonFactories.remove(beanName);
        }
    }

    /**
     * 先按参数类型获取构造器依赖，再调用唯一构造器创建对象。
     * 此时对象还未生成，无法用早期引用解决纯构造器循环依赖。
     */
    private Object createInstance(Class<?> beanClass) throws ReflectiveOperationException {
        Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
        if (constructors.length != 1) {
            throw new BeansException("Bean 只需声明一个构造器：" + beanClass.getName());
        }

        Constructor<?> constructor = constructors[0];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            arguments[i] = getBean(parameterTypes[i]);
        }

        constructor.setAccessible(true);
        return constructor.newInstance(arguments);
    }

    /**
     * 依次完成初始化前、初始化后的处理，将每次返回的对象传给下一个处理器。
     * 处理器可能返回代理，因此最终使用处理后的对象。
     */
    private Object applyBeanPostProcessors(Object bean, String beanName) {
        Object current = bean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            current = processor.postProcessBeforeInitialization(current, beanName);
        }

        for (BeanPostProcessor processor : beanPostProcessors) {
            current = processor.postProcessAfterInitialization(current, beanName);
        }

        return current;
    }

    /**
     * 按需获取已构造对象的早期引用，交给处理器决定是否包装成代理。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    private class EarlyBeanReferenceFactory {

        private final Object bean;
        private final String beanName;

        private EarlyBeanReferenceFactory(Object bean, String beanName) {
            this.bean = bean;
            this.beanName = beanName;
        }

        private Object getObject() {
            Object reference = bean;
            for (BeanPostProcessor processor : beanPostProcessors) {
                reference = processor.getEarlyBeanReference(reference, beanName);
            }

            return reference;
        }
    }
}
