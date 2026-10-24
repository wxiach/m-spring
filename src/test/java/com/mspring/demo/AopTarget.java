package com.mspring.demo;

import com.mspring.context.annotation.Component;

/**
 * AOP 的目标对象：fail 为 true 时走异常路径。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
@Component
public class AopTarget {

    public void call(boolean fail) {
        if (fail) {
            throw new RuntimeException("boom");
        }
    }
}
