package com.mspring.demo;

import com.mspring.context.annotation.Component;
import com.mspring.context.annotation.PostConstruct;
import com.mspring.context.annotation.PreDestroy;

/**
 * 构造器注入的依赖，同时记录初始化和销毁回调。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
@Component
public class CommonBean {

    public String str = "";

    @PostConstruct
    void init() {
        str += "i";
    }

    @PreDestroy
    void cleanup() {
        str += "c";
    }
}
