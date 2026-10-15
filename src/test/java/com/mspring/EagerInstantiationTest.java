package com.mspring;

import com.mspring.fixtures.eager.EagerProbe;
import com.mspring.fixtures.eager.PrototypeProbe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EagerInstantiationTest {

    @Test
    void singletonsAreCreatedAtStartup() {
        EagerProbe.COUNT.set(0);
        new ApplicationContext("com.mspring.fixtures.eager");
        assertEquals(1, EagerProbe.COUNT.get());
    }

    @Test
    void prototypesAreNotCreatedAtStartup() {
        PrototypeProbe.COUNT.set(0);
        ApplicationContext context = new ApplicationContext("com.mspring.fixtures.eager");
        assertEquals(0, PrototypeProbe.COUNT.get());
        context.getBean(PrototypeProbe.class);
        context.getBean(PrototypeProbe.class);
        assertEquals(2, PrototypeProbe.COUNT.get());
    }
}
