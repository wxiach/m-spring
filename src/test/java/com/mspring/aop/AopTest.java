package com.mspring.aop;

import com.mspring.aop.annotation.After;
import com.mspring.aop.annotation.AfterReturning;
import com.mspring.aop.annotation.AfterThrowing;
import com.mspring.aop.annotation.Around;
import com.mspring.aop.annotation.Aspect;
import com.mspring.aop.annotation.Before;
import com.mspring.beans.annotation.Autowired;
import com.mspring.beans.annotation.AutowiredAnnotationProcessor;
import com.mspring.beans.config.BeanDefinition;
import com.mspring.beans.factory.BeanFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * 验证五种通知、环绕通知嵌套和循环依赖中的代理复用。
 *
 * @author wxiach@gmail.com 2026/11/02
 */
public class AopTest {

    private static final String EXECUTE_POINTCUT =
            "execution(* com.mspring.aop.*AdvisedBean.execute(..))";
    private static final String CIRCULAR_POINTCUT =
            "execution(* com.mspring.aop.*CircularBeanA.ping(..))";

    private BeanFactory factory;

    @BeforeEach
    void setUp() {
        factory = new BeanFactory();
        factory.registerBeanDefinition(new BeanDefinition(
                "invocationLog", InvocationLog.class, BeanDefinition.SCOPE_SINGLETON));
        factory.registerBeanDefinition(new BeanDefinition(
                "advisedBean", AdvisedBean.class, BeanDefinition.SCOPE_SINGLETON));

        // 固定两个切面的登记顺序，让嵌套顺序的断言不依赖目录遍历顺序。
        factory.registerBeanDefinition(new BeanDefinition(
                "recordingAspect", RecordingAspect.class, BeanDefinition.SCOPE_SINGLETON));
        factory.registerBeanDefinition(new BeanDefinition(
                "nestedAroundAspect", NestedAroundAspect.class, BeanDefinition.SCOPE_SINGLETON));
        factory.registerBeanDefinition(new BeanDefinition(
                "circularBeanA", CircularBeanA.class, BeanDefinition.SCOPE_SINGLETON));
        factory.registerBeanDefinition(new BeanDefinition(
                "circularBeanB", CircularBeanB.class, BeanDefinition.SCOPE_SINGLETON));

        factory.addBeanPostProcessor(new AutowiredAnnotationProcessor(factory));
        factory.addBeanPostProcessor(new AspectProxyProcessor(factory));
    }

    @Test
    void invokeAdviceOnSuccessfulCall() {
        AdvisedBean bean = factory.getBean(AdvisedBean.class);
        InvocationLog log = factory.getBean(InvocationLog.class);
        RecordingAspect aspect = factory.getBean(RecordingAspect.class);

        String result = bean.execute(false);

        assertEquals("ok", result);
        assertEquals(result, aspect.returnedValue);
        assertEquals(List.of(
                "before",
                "around-start",
                "nested-around-start",
                "target",
                "nested-around-end",
                "around-end",
                "after-returning",
                "after"), log.events);
    }

    @Test
    void invokeAdviceAndRethrowOnFailedCall() {
        AdvisedBean bean = factory.getBean(AdvisedBean.class);
        InvocationLog log = factory.getBean(InvocationLog.class);
        RecordingAspect aspect = factory.getBean(RecordingAspect.class);

        try {
            bean.execute(true);
            fail("目标方法应该抛出异常");
        } catch (IllegalStateException exception) {
            assertEquals("业务方法执行失败", exception.getMessage());
            assertSame(exception, aspect.thrownException);
        }

        assertEquals(List.of(
                "before",
                "around-start",
                "nested-around-start",
                "target",
                "after-throwing",
                "after"), log.events);
    }

    @Test
    void reuseEarlyProxyForCircularSingletons() {
        CircularBeanA bean = factory.getBean(CircularBeanA.class);
        CircularBeanB dependency = factory.getBean(CircularBeanB.class);
        InvocationLog log = factory.getBean(InvocationLog.class);

        // 通过方法获取依赖，确保读取的是代理背后目标对象已注入的字段。
        assertSame(dependency, bean.getDependency());
        assertSame(bean, dependency.bean);
        assertSame(bean, factory.getBean(CircularBeanA.class));

        assertEquals("pong", dependency.bean.ping());
        assertEquals(List.of("circular-before"), log.events);
    }

    // ========================================================================
    // 测试对象：共享调用记录，直接验证通知与目标方法的执行顺序
    // ========================================================================

    /**
     * 记录所有通知和目标方法的调用，供测试统一断言。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    public static class InvocationLog {

        private final List<String> events = new ArrayList<>();
    }

    /**
     * 提供正常返回和抛出异常两条主要执行路径。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    public static class AdvisedBean {

        @Autowired
        private InvocationLog log;

        public String execute(boolean fail) {
            log.events.add("target");
            if (fail) {
                throw new IllegalStateException("业务方法执行失败");
            }

            return "ok";
        }
    }

    /**
     * 记录五种通知，并接收目标方法的返回值和原始异常。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    @Aspect
    public static class RecordingAspect {

        @Autowired
        private InvocationLog log;

        private Object returnedValue;
        private Throwable thrownException;

        @Before(EXECUTE_POINTCUT)
        public void before(JoinPoint joinPoint) {
            log.events.add("before");
        }

        @After(EXECUTE_POINTCUT)
        public void after(JoinPoint joinPoint) {
            log.events.add("after");
        }

        @AfterReturning(EXECUTE_POINTCUT)
        public void afterReturning(JoinPoint joinPoint, Object result) {
            returnedValue = result;
            log.events.add("after-returning");
        }

        @AfterThrowing(EXECUTE_POINTCUT)
        public void afterThrowing(JoinPoint joinPoint, Throwable throwable) {
            thrownException = throwable;
            log.events.add("after-throwing");
        }

        @Around(EXECUTE_POINTCUT)
        public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
            log.events.add("around-start");
            Object result = joinPoint.proceed();
            log.events.add("around-end");
            return result;
        }

        @Before(CIRCULAR_POINTCUT)
        public void beforeCircularCall(JoinPoint joinPoint) {
            log.events.add("circular-before");
        }
    }

    /**
     * 提供第二个环绕通知，验证真正的嵌套调用顺序。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    @Aspect
    public static class NestedAroundAspect {

        @Autowired
        private InvocationLog log;

        @Around(EXECUTE_POINTCUT)
        public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
            log.events.add("nested-around-start");
            Object result = joinPoint.proceed();
            log.events.add("nested-around-end");
            return result;
        }
    }

    /**
     * 循环依赖中需要被代理的单例。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    public static class CircularBeanA {

        @Autowired
        private CircularBeanB dependency;

        public CircularBeanB getDependency() {
            return dependency;
        }

        public String ping() {
            return "pong";
        }
    }

    /**
     * 持有 CircularBeanA 的早期引用。
     *
     * @author wxiach@gmail.com 2026/11/02
     */
    public static class CircularBeanB {

        @Autowired
        private CircularBeanA bean;
    }
}
