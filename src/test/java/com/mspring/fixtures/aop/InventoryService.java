package com.mspring.fixtures.aop;

import com.mspring.annotation.Component;

@Component
public class InventoryService {

    public String name() {
        return "inventory";
    }
}
