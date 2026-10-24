package com.mspring.demo;

import com.mspring.context.annotation.Component;

/**
 * 构造器注入示例。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
@Component
public record ConstructorBean(CommonBean dependency) {

}
