package com.mspring.core;

import com.mspring.exception.BeansException;
import com.mspring.processor.BeanPostProcessor;
import com.mspring.processor.DestructionAwareBeanPostProcessor;
import com.mspring.processor.InstantiationAwareBeanPostProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory extends DefaultSingletonBeanRegistry {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new BeansException("容器中不存在名为 " + beanName + " 的 Bean");
        }
        if (!beanDefinition.isSingleton()) {
            return createBean(beanDefinition);
        }
        return getSingletonBean(beanDefinition);
    }

    public <T> T getBean(Class<T> type) {
        return type.cast(resolveBeanByType(type));
    }

    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanDefinition.beanName(), beanDefinition);
    }

    public List<BeanDefinition> beanDefinitions() {
        return new ArrayList<>(beanDefinitionMap.values());
    }

    public void addBeanPostProcessor(BeanPostProcessor processor) {
        beanPostProcessors.add(processor);
    }

    public void registerProcessorBeans() {
        for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
            if (BeanPostProcessor.class.isAssignableFrom(beanDefinition.beanClass())) {
                addBeanPostProcessor((BeanPostProcessor) getBean(beanDefinition.beanName()));
            }
        }
    }

    public void preInstantiateSingletons() {
        for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
            if (beanDefinition.isSingleton()) {
                getBean(beanDefinition.beanName());
            }
        }
    }

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

    private Object getSingletonBean(BeanDefinition beanDefinition) {
        String beanName = beanDefinition.beanName();
        Object bean = getSingleton(beanName);
        if (bean != null) {
            return bean;
        }
        try {
            bean = createBean(beanDefinition);
            registerSingleton(beanName, bean);
            return bean;
        } finally {
            clearEarlyState(beanName);
        }
    }

    private Object resolveBeanByType(Class<?> type) {
        List<BeanDefinition> candidates = findBeanDefinitions(type);
        if (candidates.isEmpty()) {
            throw new BeansException("容器中不存在 " + type.getName() + " 类型的 Bean");
        }
        if (candidates.size() == 1) {
            return getBean(candidates.get(0).beanName());
        }
        throw new BeansException("容器中存在多个 " + type.getName() + " 类型的 Bean："
                + candidates.stream().map(BeanDefinition::beanName).toList());
    }

    private List<BeanDefinition> findBeanDefinitions(Class<?> type) {
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
            if (type.isAssignableFrom(beanDefinition.beanClass())) {
                beanDefinitions.add(beanDefinition);
            }
        }
        return beanDefinitions;
    }

    private Object createBean(BeanDefinition beanDefinition) {
        String beanName = beanDefinition.beanName();
        if (!beforeBeanCreation(beanName)) {
            throw new BeansException("检测到循环依赖：" + beanName + " 正在创建中，无法再次创建。"
                    + "字段注入的循环依赖可以通过提前暴露解决，构造器循环依赖无解");
        }
        Class<?> beanClass = beanDefinition.beanClass();
        try {
            Constructor<?> constructor = chooseConstructor(beanClass);
            Object[] args = resolveArguments(constructor.getParameters());
            constructor.setAccessible(true);
            Object bean = constructor.newInstance(args);
            if (beanDefinition.isSingleton()) {
                addSingletonFactory(beanName, () -> getEarlyBeanReference(bean, beanName));
            }
            populateBean(bean, beanName);
            return initializeBean(bean, beanName);
        } catch (InvocationTargetException e) {
            throw new BeansException("实例化 Bean 失败，构造器内部抛出异常：" + beanClass.getName(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new BeansException("实例化 Bean 失败：" + beanClass.getName(), e);
        } finally {
            afterBeanCreation(beanName);
        }
    }

    private Constructor<?> chooseConstructor(Class<?> beanClass) {
        Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }
        try {
            return beanClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new BeansException(beanClass.getName() + " 没有可用的构造器：存在多个构造器且没有无参构造器", e);
        }
    }

    private Object[] resolveArguments(Parameter[] parameters) {
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            args[i] = resolveDependency(parameters[i].getType());
        }
        return args;
    }

    public Object resolveDependency(Class<?> type) {
        return resolveBeanByType(type);
    }

    private void populateBean(Object bean, String beanName) {
        for (BeanPostProcessor processor : beanPostProcessors) {
            if (processor instanceof InstantiationAwareBeanPostProcessor aware) {
                aware.postProcessProperties(bean, beanName);
            }
        }
    }

    private Object getEarlyBeanReference(Object bean, String beanName) {
        Object current = bean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            if (processor instanceof InstantiationAwareBeanPostProcessor aware) {
                current = aware.getEarlyBeanReference(current, beanName);
            }
        }
        return current;
    }

    private Object initializeBean(Object bean, String beanName) {
        Object current = bean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            current = processor.postProcessBeforeInitialization(current, beanName);
        }
        for (BeanPostProcessor processor : beanPostProcessors) {
            current = processor.postProcessAfterInitialization(current, beanName);
        }
        return current;
    }
}
