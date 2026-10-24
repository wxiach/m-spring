package com.mspring.beans.annotation;

import com.mspring.beans.BeanCreationException;
import com.mspring.beans.factory.BeanFactory;
import com.mspring.beans.config.InstantiationAwareBeanPostProcessor;
import com.mspring.core.AnnotationUtils;
import com.mspring.core.Ordered;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 处理 @Autowired 字段注入：沿继承链找到标注字段，按字段类型从容器解析依赖并注入。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class AutowiredAnnotationProcessor implements InstantiationAwareBeanPostProcessor, Ordered {

    private final BeanFactory beanFactory;

    public AutowiredAnnotationProcessor(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessProperties(Object bean, String beanName) {
        for (Field field : AnnotationUtils.findFieldsAnnotatedWith(bean.getClass(), Autowired.class)) {
            injectField(bean, field);
        }
    }

    private void injectField(Object bean, Field field) {
        if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
            return;
        }
        try {
            field.setAccessible(true);
            // 根据字段类型解析依赖并注入，如果没有找到对应的Bean或找到多个则抛出异常
            field.set(bean, beanFactory.getBean(field.getType()));
        } catch (IllegalAccessException e) {
            throw new BeanCreationException("注入依赖失败：" + field.getDeclaringClass().getName() + "#" + field.getName(), e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 2;
    }
}
