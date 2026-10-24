package com.mspring.aop;

import com.mspring.aop.Advisor.AdviceType;
import com.mspring.aop.annotation.Aspect;
import com.mspring.beans.config.BeanDefinition;
import com.mspring.beans.config.InstantiationAwareBeanPostProcessor;
import com.mspring.beans.factory.BeanFactory;
import com.mspring.core.AnnotationUtils;
import com.mspring.core.Ordered;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AOP 代理处理器：为匹配切点的 Bean 创建代理；循环依赖场景下通过早期引用提前生成代理。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public class AspectProxyProcessor implements InstantiationAwareBeanPostProcessor, Ordered {

    private final BeanFactory beanFactory;
    private final Map<String, Object> proxyCache = new ConcurrentHashMap<>();
    private final List<Advisor> advisors;

    public AspectProxyProcessor(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        // 构造时构建通知列表：此刻本处理器尚未注册，getBean 不会回调到这里
        this.advisors = buildAdvisors();
    }

    // ---------------------------------------------------------------------
    // BeanPostProcessor 回调
    // ---------------------------------------------------------------------

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return wrapIfNecessary(bean, beanName);
    }

    @Override
    public Object getEarlyBeanReference(Object bean, String beanName) {
        return wrapIfNecessary(bean, beanName);
    }

    // ---------------------------------------------------------------------
    // 代理包装
    // ---------------------------------------------------------------------

    private Object wrapIfNecessary(Object bean, String beanName) {
        Object cached = proxyCache.get(beanName);
        if (cached != null) {
            return cached;
        }
        Class<?> beanClass = bean.getClass();
        // 切面自身不代理
        if (beanClass.isAnnotationPresent(Aspect.class)) {
            return bean;
        }
        List<Advisor> matched = advisors.stream().filter(advisor -> advisor.pointcut().matches(beanClass)).toList();
        if (matched.isEmpty()) {
            return bean;
        }
        // 只缓存生成的代理，未代理的 Bean（切面、无匹配、原型）不进缓存
        Object proxy = ProxyFactory.createProxy(bean, matched);
        proxyCache.put(beanName, proxy);
        return proxy;
    }

    // ---------------------------------------------------------------------
    // 通知器（Advisor）构建
    // ---------------------------------------------------------------------

    private List<Advisor> buildAdvisors() {
        List<Advisor> result = new ArrayList<>();
        for (BeanDefinition definition : beanFactory.getBeanDefinitions()) {
            if (!definition.beanClass().isAnnotationPresent(Aspect.class)) {
                continue;
            }
            result.addAll(advisorsOf(definition));
        }
        return result;
    }

    // 收集一个切面类上的全部通知
    private List<Advisor> advisorsOf(BeanDefinition definition) {
        Object aspect = beanFactory.getBean(definition.beanName());
        List<Advisor> advisors = new ArrayList<>();
        for (AdviceType type : AdviceType.values()) {
            for (Method method : AnnotationUtils.findMethodsAnnotatedWith(definition.beanClass(), type.annotation())) {
                // 启动时校验通知方法签名，运行期不再检查
                if (!type.accepts(method)) {
                    throw new IllegalStateException(type + " 通知方法签名不符合固定参数约定：" + method);
                }
                advisors.add(new Advisor(new PointcutExpression(type.pointcutOf(method)), type, aspect, method));
            }
        }
        return advisors;
    }
}
