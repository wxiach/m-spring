package com.mspring;

import com.mspring.exception.BeansException;
import com.mspring.fixtures.circular.field.CircularA;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CircularDependencyTest {

    private final ApplicationContext context = new ApplicationContext("com.mspring.fixtures.circular.field");

    @Test
    void resolvesFieldCircularDependency() {
        CircularA a = context.getBean(CircularA.class);
        assertSame(a, a.getB().getA());
    }

    @Test
    void constructorCircularDependencyFailsFastAtStartup() {
        assertThrows(BeansException.class,
                () -> new ApplicationContext("com.mspring.fixtures.circular.constructor"));
    }
}
