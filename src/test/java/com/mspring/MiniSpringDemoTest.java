package com.mspring;

import com.mspring.context.ApplicationContext;
import com.mspring.context.ClassPathApplicationContext;
import com.mspring.demo.AopAspect;
import com.mspring.demo.AopTarget;
import com.mspring.demo.CommonBean;
import com.mspring.demo.PrototypeBean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * m-spring 学习 demo：一次启动覆盖扫描、DI、单例、循环依赖、原型作用域、AOP 和生命周期。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
class MiniSpringDemoTest {
    @Test
    void runs() {
        // 启动容器：组件扫描、构造器注入、字段循环依赖和单例初始化都在这里完成
        ApplicationContext context = new ClassPathApplicationContext("com.mspring.demo");

        // 原型作用域：每次获取都应该是新实例
        assertNotSame(context.getBean(PrototypeBean.class), context.getBean(PrototypeBean.class));

        // AOP：先走正常路径，再走异常路径
        AopTarget target = context.getBean(AopTarget.class);
        AopAspect aspect = context.getBean(AopAspect.class);
        target.call(false);
        assertThrows(RuntimeException.class, () -> target.call(true));
        assertEquals("biorbit", aspect.str);

        // 生命周期：关闭容器后，init 和 cleanup 都应该执行
        CommonBean commonBean = context.getBean(CommonBean.class);
        context.close();
        assertEquals("ic", commonBean.str);
    }
}
