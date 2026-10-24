package com.mspring.beans.factory;

import com.mspring.beans.NoSuchBeanDefinitionException;
import com.mspring.beans.NoUniqueBeanDefinitionException;
import com.mspring.beans.config.BeanDefinition;
import com.mspring.beans.config.BeanDefinitionRegistry;
import com.mspring.beans.config.BeanPostProcessor;
import com.mspring.core.Ordered;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BeanFactory 的默认实现：持有 BeanDefinition 注册表，负责按名称/类型获取 Bean、
 * 处理器的注册排序与单例的预实例化。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class DefaultBeanFactory extends AbstractBeanFactory implements BeanDefinitionRegistry {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    // ---------------------------------------------------------------------
    // BeanDefinitionRegistry 实现
    // ---------------------------------------------------------------------

    @Override
    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanDefinition.beanName(), beanDefinition);
    }

    @Override
    public List<BeanDefinition> getBeanDefinitions() {
        return new ArrayList<>(beanDefinitionMap.values());
    }

    // ---------------------------------------------------------------------
    // Bean 获取
    // ---------------------------------------------------------------------

    @Override
    public Object getBean(String name) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        if (beanDefinition == null) {
            throw new NoSuchBeanDefinitionException("容器中不存在名为 " + name + " 的 Bean");
        }
        // 单例先查缓存（getSingleton 内部处理循环依赖的早期引用），原型每次新建
        if (beanDefinition.isSingleton()) {
            return getSingleton(name, () -> createBean(beanDefinition));
        }
        return createBean(beanDefinition);
    }

    @Override
    public <T> T getBean(Class<T> type) {
        List<BeanDefinition> candidates = new ArrayList<>();
        for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
            if (type.isAssignableFrom(beanDefinition.beanClass())) {
                candidates.add(beanDefinition);
            }
        }
        if (candidates.size() != 1) {
            throw new NoUniqueBeanDefinitionException("无法唯一确定 " + type.getName() + " 类型的 Bean");
        }
        return type.cast(getBean(candidates.get(0).beanName()));
    }

    // ---------------------------------------------------------------------
    // 容器生命周期
    // ---------------------------------------------------------------------

    public void registerBeanPostProcessors() {
        for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
            if (BeanPostProcessor.class.isAssignableFrom(beanDefinition.beanClass())) {
                addBeanPostProcessor((BeanPostProcessor) getBean(beanDefinition.beanName()));
            }
        }
        // 内置处理器与容器扫描发现的处理器合并后按 Ordered 稳定排序
        getBeanPostProcessors().sort(Comparator.comparingInt(processor -> {
            if (processor instanceof Ordered ordered) {
                return ordered.getOrder();
            }
            return Ordered.LOWEST_PRECEDENCE;
        }));
    }

    public void preInstantiateSingletons() {
        for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
            if (beanDefinition.isSingleton()) {
                getBean(beanDefinition.beanName());
            }
        }
    }
}
