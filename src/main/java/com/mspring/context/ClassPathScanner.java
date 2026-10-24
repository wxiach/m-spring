package com.mspring.context;

import com.mspring.beans.BeansException;
import com.mspring.beans.config.BeanDefinition;
import com.mspring.beans.config.BeanDefinitionRegistry;
import com.mspring.context.annotation.Component;
import com.mspring.context.annotation.Scope;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.stream.Stream;

/**
 * 类路径组件扫描器：扫描标注了 @Component 的类并注册为 BeanDefinition。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class ClassPathScanner {

    private final ClassLoader classLoader;

    public ClassPathScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void scan(BeanDefinitionRegistry registry, String... packageNames) {
        for (String packageName : packageNames) {
            try {
                Enumeration<URL> resources = classLoader.getResources(packageName.replace(".", "/"));
                while (resources.hasMoreElements()) {
                    scanDirectory(registry, Path.of(resources.nextElement().toURI()), packageName);
                }
            } catch (IOException | URISyntaxException e) {
                throw new BeansException("扫描包失败：" + packageName, e);
            }
        }
    }

    private void scanDirectory(BeanDefinitionRegistry registry, Path root, String packageName) {
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(path -> path.toString().endsWith(".class"))
                    .map(root::relativize)
                    .map(relative -> packageName + "." + relative.toString()
                            .replace(File.separatorChar, '.')
                            .replace(".class", ""))
                    .forEach(className -> registerComponent(registry, className));
        } catch (IOException e) {
            throw new BeansException("扫描目录失败：" + root, e);
        }
    }

    private void registerComponent(BeanDefinitionRegistry registry, String className) {
        Class<?> clazz;
        try {
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new BeansException("类加载失败：" + className, e);
        }
        if (!clazz.isAnnotationPresent(Component.class)) {
            return;
        }
        // 未显式命名时，默认 bean 名为首字母小写的简单类名
        Component component = clazz.getAnnotation(Component.class);
        String beanName = component.value().isEmpty()
                ? Introspector.decapitalize(clazz.getSimpleName())
                : component.value();
        Scope scope = clazz.getAnnotation(Scope.class);
        registry.registerBeanDefinition(
                new BeanDefinition(beanName, clazz, scope == null ? BeanDefinition.SCOPE_SINGLETON : scope.value()));
    }
}
