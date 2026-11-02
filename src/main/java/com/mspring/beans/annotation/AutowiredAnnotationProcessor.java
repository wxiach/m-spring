package com.mspring.beans.annotation;

import com.mspring.beans.BeansException;
import com.mspring.beans.config.BeanPostProcessor;
import com.mspring.beans.factory.BeanFactory;
import com.mspring.core.AnnotationUtils;

import java.lang.reflect.Field;

/**
 * 字段注入：找到 @Autowired 字段，按类型获取依赖，再写入字段。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public class AutowiredAnnotationProcessor implements BeanPostProcessor {

    private final BeanFactory beanFactory;

    public AutowiredAnnotationProcessor(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessProperties(Object bean, String beanName) {
        try {
            for (Field field : AnnotationUtils.findFieldsAnnotatedWith(bean.getClass(), Autowired.class)) {
                field.setAccessible(true);
                field.set(bean, beanFactory.getBean(field.getType()));
            }
        } catch (Exception exception) {
            throw new BeansException("字段注入失败：" + beanName, exception);
        }
    }
}
