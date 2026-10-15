package com.mspring.core;

import com.mspring.annotation.Component;
import com.mspring.annotation.Scope;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.stream.Stream;

public class ClassPathScanner {

    private final ClassLoader classLoader;

    public ClassPathScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void scan(BeanFactory beanFactory, String... packageNames) {
        for (String packageName : packageNames) {
            try {
                Enumeration<URL> resources = classLoader.getResources(packageName.replace(".", "/"));
                while (resources.hasMoreElements()) {
                    scanDirectory(beanFactory, Path.of(resources.nextElement().toURI()), packageName);
                }
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException("扫描包失败：" + packageName, e);
            }
        }
    }

    private void scanDirectory(BeanFactory beanFactory, Path root, String packageName) {
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(path -> path.toString().endsWith(".class"))
                    .map(root::relativize)
                    .map(relative -> packageName + "." + relative.toString()
                            .replace(File.separatorChar, '.')
                            .replace(".class", ""))
                    .forEach(className -> registerComponent(beanFactory, className));
        } catch (IOException e) {
            throw new RuntimeException("扫描目录失败：" + root, e);
        }
    }

    private void registerComponent(BeanFactory beanFactory, String className) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            if (clazz.isAnnotationPresent(Component.class)) {
                beanFactory.registerBeanDefinition(new BeanDefinition(resolveBeanName(clazz), clazz, resolveScope(clazz)));
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("类加载失败：" + className, e);
        }
    }

    private String resolveBeanName(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        if (component != null && !component.value().isEmpty()) {
            return component.value();
        }
        return Introspector.decapitalize(clazz.getSimpleName());
    }

    private String resolveScope(Class<?> clazz) {
        Scope scope = clazz.getAnnotation(Scope.class);
        return scope == null ? "singleton" : scope.value();
    }
}
