package com.mspring;

import com.mspring.aop.AspectProxyProcessor;
import com.mspring.core.BeanFactory;
import com.mspring.core.ClassPathScanner;
import com.mspring.exception.BeansException;
import com.mspring.processor.AutowiredAnnotationProcessor;
import com.mspring.processor.CommonAnnotationProcessor;

public class ApplicationContext {

    private final BeanFactory beanFactory = new BeanFactory();

    public ApplicationContext(String... packageNames) {
        new ClassPathScanner(Thread.currentThread().getContextClassLoader()).scan(beanFactory, packageNames);
        refresh();
    }

    public void refresh() {
        try {
            beanFactory.addBeanPostProcessor(new AutowiredAnnotationProcessor(beanFactory));
            beanFactory.addBeanPostProcessor(new CommonAnnotationProcessor());
            beanFactory.addBeanPostProcessor(new AspectProxyProcessor(beanFactory));
            beanFactory.registerProcessorBeans();
            beanFactory.preInstantiateSingletons();
        } catch (BeansException e) {
            beanFactory.destroySingletons();
            throw e;
        }
    }

    public Object getBean(String beanName) {
        return beanFactory.getBean(beanName);
    }

    public <T> T getBean(Class<T> beanClass) {
        return beanFactory.getBean(beanClass);
    }

    public void close() {
        beanFactory.destroySingletons();
    }
}
