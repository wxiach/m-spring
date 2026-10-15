package com.mspring.fixtures.lifecycle;

import com.mspring.annotation.Component;
import com.mspring.processor.BeanPostProcessor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class RecordingProcessor implements BeanPostProcessor {

    public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        EVENTS.add("before:" + beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        EVENTS.add("after:" + beanName);
        return bean;
    }
}
