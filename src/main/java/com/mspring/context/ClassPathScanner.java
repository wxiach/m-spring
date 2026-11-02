package com.mspring.context;

import com.mspring.beans.BeansException;
import com.mspring.beans.config.BeanDefinition;
import com.mspring.beans.factory.BeanFactory;
import com.mspring.context.annotation.Component;
import com.mspring.context.annotation.Scope;

import java.beans.Introspector;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;

/**
 * 扫描本地 class 目录，把 @Component 类登记为 Bean 定义。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public class ClassPathScanner {

    private final ClassLoader classLoader;

    public ClassPathScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 扫描指定包及其子包，将标注 {@code @Component} 的类登记到工厂。
     */
    public void scan(BeanFactory beanFactory, String... packageNames) {
        for (String packageName : packageNames) {
            try {
                Enumeration<URL> resources = classLoader.getResources(packageName.replace('.', '/'));

                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    scanDirectory(beanFactory, new File(resource.toURI()), packageName);
                }
            } catch (Exception exception) {
                throw new BeansException("扫描包失败：" + packageName, exception);
            }
        }
    }

    private void scanDirectory(BeanFactory beanFactory, File directory, String packageName)
            throws ClassNotFoundException {
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(beanFactory, file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String simpleName = file.getName().substring(0, file.getName().length() - 6);
                registerComponent(beanFactory, packageName + "." + simpleName);
            }
        }
    }

    private void registerComponent(BeanFactory beanFactory, String className) throws ClassNotFoundException {
        Class<?> beanClass = classLoader.loadClass(className);
        Component component = beanClass.getAnnotation(Component.class);
        if (component == null) {
            return;
        }

        String beanName = component.value();
        if (beanName.isEmpty()) {
            beanName = Introspector.decapitalize(beanClass.getSimpleName());
        }

        Scope scope = beanClass.getAnnotation(Scope.class);
        String scopeName = BeanDefinition.SCOPE_SINGLETON;
        if (scope != null) {
            scopeName = scope.value();
        }

        beanFactory.registerBeanDefinition(new BeanDefinition(beanName, beanClass, scopeName));
    }
}
