package com.mspring.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 注解查找工具：沿继承链查找标注了指定注解的方法与字段。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
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

    public static List<Field> findFieldsAnnotatedWith(Class<?> clazz, Class<? extends Annotation> annotationType) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotationType)) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }
}
