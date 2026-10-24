package com.mspring.context;

import com.mspring.beans.factory.BeanFactory;

/**
 * 应用上下文入口：继承 BeanFactory 的查询能力，close() 销毁所有单例。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public interface ApplicationContext extends BeanFactory, AutoCloseable {

    @Override
    void close();
}
