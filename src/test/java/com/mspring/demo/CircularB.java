package com.mspring.demo;

import com.mspring.beans.annotation.Autowired;
import com.mspring.context.annotation.Component;

/**
 * 与 CircularA 互相字段依赖，验证循环依赖可解
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
@Component
public class CircularB {

    @Autowired
    private CircularA a;

    public CircularA getA() {
        return a;
    }
}
