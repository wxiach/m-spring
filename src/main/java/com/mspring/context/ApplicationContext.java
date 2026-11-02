package com.mspring.context;

import com.mspring.aop.AspectProxyProcessor;
import com.mspring.beans.annotation.AutowiredAnnotationProcessor;
import com.mspring.beans.config.BeanDefinition;
import com.mspring.beans.factory.BeanFactory;

import java.util.List;

/**
 * 容器入口：扫描组件、注册依赖注入和 AOP 处理器，然后创建单例。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public class ApplicationContext {

    private final BeanFactory beanFactory = new BeanFactory();

    /**
     * 扫描指定包，注册字段注入和 AOP 处理器，然后创建单例。
     */
    public ApplicationContext(String... packageNames) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        new ClassPathScanner(classLoader).scan(beanFactory, packageNames);

        beanFactory.addBeanPostProcessor(new AutowiredAnnotationProcessor(beanFactory));
        beanFactory.addBeanPostProcessor(new AspectProxyProcessor(beanFactory));

        // 处理器全部就绪后再创建单例，让依赖注入和 AOP 参与 Bean 的创建。
        beanFactory.preInstantiateSingletons();
    }

    /**
     * 按名称获取 Bean。
     */
    public Object getBean(String name) {
        return beanFactory.getBean(name);
    }

    /**
     * 按类型获取唯一匹配的 Bean。
     */
    public <T> T getBean(Class<T> type) {
        return beanFactory.getBean(type);
    }

    /**
     * 返回容器中已登记的 Bean 定义。
     */
    public List<BeanDefinition> getBeanDefinitions() {
        return beanFactory.getBeanDefinitions();
    }
}
