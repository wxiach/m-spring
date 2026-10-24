package com.mspring.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AnnotationUtils {

    private AnnotationUtils() {
    }

    public static List<Method> findMethodsAnnotatedWith(Class<?> clazz, Class<? extends Annotation> annotationType) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotationType)) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }
}
