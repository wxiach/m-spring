package com.mspring;

import com.mspring.fixtures.lifecycle.RecordingProcessor;
import com.mspring.fixtures.lifecycle.ReplacementTarget;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanPostProcessorTest {

    private final ApplicationContext context = new ApplicationContext("com.mspring.fixtures.lifecycle");

    @Test
    void processorSeesBeansDuringStartup() {
        assertTrue(RecordingProcessor.EVENTS.contains("before:replacementTarget"));
    }

    @Test
    void beforeRunsBeforeAfter() {
        List<String> events = RecordingProcessor.EVENTS;
        assertTrue(events.indexOf("before:replacementTarget") < events.indexOf("after:replacementTarget"));
    }

    @Test
    void processorCanReplaceBean() {
        assertEquals("replaced", context.getBean(ReplacementTarget.class).value());
    }
}
