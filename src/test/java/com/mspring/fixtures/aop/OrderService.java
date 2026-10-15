package com.mspring.fixtures.aop;

import com.mspring.annotation.Autowired;
import com.mspring.annotation.Component;

@Component
public class OrderService {

    @Autowired
    private InventoryService inventoryService;

    public String createOrder(String item) {
        return "order:" + item;
    }

    public String queryOrder() {
        return "query";
    }

    public void fail() {
        throw new RuntimeException("boom");
    }

    public String inventory() {
        return inventoryService.name();
    }
}
