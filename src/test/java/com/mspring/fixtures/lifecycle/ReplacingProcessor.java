package com.mspring.fixtures.lifecycle;

import com.mspring.annotation.Component;
import com.mspring.processor.BeanPostProcessor;

@Component
public class ReplacingProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof ReplacementTarget) {
            return new ReplacementTarget("replaced");
        }
        return bean;
    }
}
