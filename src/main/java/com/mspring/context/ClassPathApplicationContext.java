package com.mspring.context;

import com.mspring.aop.AspectProxyProcessor;
import com.mspring.beans.BeansException;
import com.mspring.beans.annotation.AutowiredAnnotationProcessor;
import com.mspring.beans.config.BeanDefinition;
import com.mspring.beans.factory.DefaultBeanFactory;
import com.mspring.context.annotation.CommonAnnotationProcessor;

import java.util.List;

/**
 * ApplicationContext 的类路径扫描实现：构造时扫描指定包并刷新容器。
 * 支持 try-with-resources 自动销毁单例。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class ClassPathApplicationContext implements ApplicationContext {

    private final DefaultBeanFactory beanFactory = new DefaultBeanFactory();

    public ClassPathApplicationContext(String... packageNames) {
        new ClassPathScanner(Thread.currentThread().getContextClassLoader()).scan(beanFactory, packageNames);
        refresh();
    }

    /**
     * 刷新容器：注册内置处理器 → 发现并注册容器中的处理器 → 预实例化单例。
     */
    public void refresh() {
        try {
            prepareBeanFactory();
            registerBeanPostProcessors();
            finishBeanFactoryInitialization();
        } catch (BeansException e) {
            beanFactory.destroySingletons();
            throw e;
        }
    }

    /**
     * 注册容器内置的 BeanPostProcessor（依赖注入、生命周期回调、AOP 代理）
     */
    private void prepareBeanFactory() {
        beanFactory.addBeanPostProcessor(new AutowiredAnnotationProcessor(beanFactory));
        beanFactory.addBeanPostProcessor(new CommonAnnotationProcessor());
        beanFactory.addBeanPostProcessor(new AspectProxyProcessor(beanFactory));
    }

    /**
     * 发现并注册容器中声明的 BeanPostProcessor，与内置处理器合并排序
     */
    private void registerBeanPostProcessors() {
        beanFactory.registerBeanPostProcessors();
    }

    /**
     * 预实例化所有单例 Bean
     */
    private void finishBeanFactoryInitialization() {
        beanFactory.preInstantiateSingletons();
    }

    @Override
    public Object getBean(String name) {
        return beanFactory.getBean(name);
    }

    @Override
    public <T> T getBean(Class<T> type) {
        return beanFactory.getBean(type);
    }

    @Override
    public List<BeanDefinition> getBeanDefinitions() {
        return beanFactory.getBeanDefinitions();
    }

    @Override
    public void close() {
        beanFactory.destroySingletons();
    }
}
