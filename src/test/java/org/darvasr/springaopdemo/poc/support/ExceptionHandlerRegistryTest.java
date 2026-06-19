package org.darvasr.springaopdemo.poc.support;

import org.darvasr.springaopdemo.poc.annotation.GlobalExceptionHandler;
import org.darvasr.springaopdemo.poc.annotation.Handles;
import org.darvasr.springaopdemo.poc.support.ExceptionHandlerRegistry.HandlerMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the dispatcher's most-specific matching logic over an artificial
 * exception hierarchy (Req 5.3, 10.3).
 */
class ExceptionHandlerRegistryTest {

    static class BaseEx extends RuntimeException {
        BaseEx(String m) {
            super(m);
        }
    }

    static class DerivedEx extends BaseEx {
        DerivedEx(String m) {
            super(m);
        }
    }

    static class GrandChildEx extends DerivedEx {
        GrandChildEx(String m) {
            super(m);
        }
    }

    static class UnrelatedEx extends RuntimeException {
        UnrelatedEx(String m) {
            super(m);
        }
    }

    @GlobalExceptionHandler
    static class TestHandlers {

        @Handles(BaseEx.class)
        public String onBase(BaseEx ex) {
            return "base";
        }

        @Handles(DerivedEx.class)
        public String onDerived(DerivedEx ex) {
            return "derived";
        }
    }

    private ExceptionHandlerRegistry registry;

    @BeforeEach
    void setUp() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(TestHandlers.class);
        context.refresh();
        registry = new ExceptionHandlerRegistry(context);
        registry.registerHandlers();
    }

    @Test
    void directMatchSelectsExactHandler() {
        Optional<HandlerMethod> handler = registry.resolve(DerivedEx.class);
        assertTrue(handler.isPresent());
        assertEquals("onDerived", handler.get().method().getName());
    }

    @Test
    void baseTypeSelectsBaseHandler() {
        Optional<HandlerMethod> handler = registry.resolve(BaseEx.class);
        assertTrue(handler.isPresent());
        assertEquals("onBase", handler.get().method().getName());
    }

    @Test
    void subtypeWithoutOwnHandlerSelectsMostSpecificAncestor() {
        Optional<HandlerMethod> handler = registry.resolve(GrandChildEx.class);
        assertTrue(handler.isPresent());
        // The closest ancestor is DerivedEx, not BaseEx.
        assertEquals("onDerived", handler.get().method().getName());
    }

    @Test
    void unrelatedTypeResolvesToEmpty() {
        assertTrue(registry.resolve(UnrelatedEx.class).isEmpty());
    }
}
