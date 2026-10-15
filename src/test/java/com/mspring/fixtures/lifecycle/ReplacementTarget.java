package com.mspring.fixtures.lifecycle;

import com.mspring.annotation.Component;

@Component
public class ReplacementTarget {

    private final String value;

    public ReplacementTarget() {
        this("original");
    }

    public ReplacementTarget(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
