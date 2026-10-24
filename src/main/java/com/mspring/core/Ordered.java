package com.mspring.core;

/**
 * 优先级契约：值越小优先级越高。
 *
 * @author wxiach <wxiach@gmail.com> 2026/10/23
 */
public interface Ordered {

    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    int getOrder();
}
