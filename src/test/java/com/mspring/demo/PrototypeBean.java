package com.mspring.demo;

import com.mspring.context.annotation.Component;
import com.mspring.context.annotation.Scope;

/**
 * 原型作用域：每次 getBean 都创建新实例。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
@Component
@Scope("prototype")
public class PrototypeBean {
}
