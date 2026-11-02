package com.mspring.ioc;

import com.mspring.beans.annotation.Autowired;
import com.mspring.beans.annotation.AutowiredAnnotationProcessor;
import com.mspring.beans.config.BeanDefinition;
import com.mspring.beans.config.BeanPostProcessor;
import com.mspring.beans.factory.BeanFactory;
import com.mspring.context.ApplicationContext;
import com.mspring.context.annotation.Component;
import com.mspring.context.annotation.Scope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 验证组件扫描、依赖注入、作用域和三级缓存的主要行为。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public class IocTest {

    @Test
    void getSingletonByNameAndType() {
        ApplicationContext context = new ApplicationContext("com.mspring.ioc");

        Dependency dependency = context.getBean(Dependency.class);

        assertSame(dependency, context.getBean("dependency"));
        assertSame(dependency, context.getBean(Dependency.class));
    }

    @Test
    void injectConstructorAndFieldDependencies() {
        ApplicationContext context = new ApplicationContext("com.mspring.ioc");

        Dependency dependency = context.getBean(Dependency.class);
        ConstructorInjectedBean constructorBean = context.getBean(ConstructorInjectedBean.class);
        FieldInjectedBean fieldBean = context.getBean(FieldInjectedBean.class);

        assertSame(dependency, constructorBean.dependency);
        assertSame(dependency, fieldBean.dependency);
    }

    @Test
    void createPrototypeForEachRequest() {
        ApplicationContext context = new ApplicationContext("com.mspring.ioc");

        PrototypeBean first = context.getBean(PrototypeBean.class);
        PrototypeBean second = context.getBean(PrototypeBean.class);

        assertNotSame(first, second);
    }

    @Test
    void resolveCircularSingletonsWithOneEarlyReference() {
        BeanFactory factory = new BeanFactory();
        factory.registerBeanDefinition(new BeanDefinition(
                "dependency", Dependency.class, BeanDefinition.SCOPE_SINGLETON));
        factory.registerBeanDefinition(new BeanDefinition(
                "circularBeanA", CircularBeanA.class, BeanDefinition.SCOPE_SINGLETON));
        factory.registerBeanDefinition(new BeanDefinition(
                "circularBeanB", CircularBeanB.class, BeanDefinition.SCOPE_SINGLETON));

        EarlyReferenceCounter counter = new EarlyReferenceCounter();
        factory.addBeanPostProcessor(new AutowiredAnnotationProcessor(factory));
        factory.addBeanPostProcessor(counter);

        // 普通单例直接完成创建，不触发早期引用工厂。
        factory.getBean(Dependency.class);
        assertEquals(0, counter.calls);

        // 明确从 A 开始创建，验证 A → B → A 时只生成一次早期引用。
        CircularBeanA bean = factory.getBean(CircularBeanA.class);

        assertSame(bean, bean.dependency.dependency);
        assertSame(bean, factory.getBean(CircularBeanA.class));
        assertSame(bean.dependency, factory.getBean(CircularBeanB.class));
        assertEquals(1, counter.calls);
    }

    // ========================================================================
    // 测试对象：只提供当前测试需要的字段、构造器和作用域
    // ========================================================================

    /**
     * 用于验证单例获取和按类型注入的依赖对象。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    @Component
    public static class Dependency {
    }

    /**
     * 通过唯一构造器接收依赖。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    @Component
    public static class ConstructorInjectedBean {

        private final Dependency dependency;

        public ConstructorInjectedBean(Dependency dependency) {
            this.dependency = dependency;
        }
    }

    /**
     * 通过字段注解接收依赖。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    @Component
    public static class FieldInjectedBean {

        @Autowired
        private Dependency dependency;
    }

    /**
     * 每次获取时创建新实例的原型对象。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    @Component
    @Scope("prototype")
    public static class PrototypeBean {
    }

    /**
     * 与 CircularBeanB 构成单例字段循环依赖。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    @Component
    public static class CircularBeanA {

        @Autowired
        private CircularBeanB dependency;
    }

    /**
     * 将依赖指回 CircularBeanA，触发早期引用获取。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    @Component
    public static class CircularBeanB {

        @Autowired
        private CircularBeanA dependency;
    }

    /**
     * 观察早期引用工厂的实际调用次数。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    private static class EarlyReferenceCounter implements BeanPostProcessor {

        private int calls;

        @Override
        public Object getEarlyBeanReference(Object bean, String beanName) {
            calls++;
            return bean;
        }
    }
}
