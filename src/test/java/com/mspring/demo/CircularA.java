package com.mspring.demo;

import com.mspring.beans.annotation.Autowired;
import com.mspring.context.annotation.Component;

/**
 * 与 CircularB 互相字段依赖，验证循环依赖可解
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
@Component
public class CircularA {

    @Autowired
    private CircularB b;

    public CircularB getB() {
        return b;
    }
}
