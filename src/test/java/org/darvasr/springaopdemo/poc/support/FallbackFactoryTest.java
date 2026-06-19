package org.darvasr.springaopdemo.poc.support;

import org.darvasr.springaopdemo.model.Order;
import org.darvasr.springaopdemo.model.Receipt;
import org.darvasr.springaopdemo.model.Report;
import org.darvasr.springaopdemo.model.StockLevel;
import org.darvasr.springaopdemo.model.Summary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link FallbackFactory} (Req 8.1, 8.2, 8.3).
 */
class FallbackFactoryTest {

    private final FallbackFactory factory = new FallbackFactory();

    @Test
    void registeredTypesReturnNonNullFallbackInstances() {
        assertTrue(((Order) factory.createFallback(Order.class)).fallback());
        assertTrue(((Receipt) factory.createFallback(Receipt.class)).fallback());
        assertTrue(((StockLevel) factory.createFallback(StockLevel.class)).fallback());
        assertTrue(((Report) factory.createFallback(Report.class)).fallback());
        assertTrue(((Summary) factory.createFallback(Summary.class)).fallback());
    }

    @Test
    void registeredFallbackIsInstanceOfRequestedType() {
        assertInstanceOf(Order.class, factory.createFallback(Order.class));
        assertInstanceOf(Report.class, factory.createFallback(Report.class));
    }

    @Test
    void voidAndVoidBoxedReturnNull() {
        assertNull(factory.createFallback(void.class));
        assertNull(factory.createFallback(Void.class));
    }

    @Test
    void unregisteredReferenceTypeReturnsNull() {
        assertNull(factory.createFallback(String.class));
    }

    @Test
    void primitiveTypeReturnsDefault() {
        assertEquals(0, factory.createFallback(int.class));
        assertEquals(false, factory.createFallback(boolean.class));
        assertEquals(0L, factory.createFallback(long.class));
    }

    @Test
    void createFallbackIsDeterministic() {
        assertEquals(factory.createFallback(Order.class), factory.createFallback(Order.class));
        assertEquals(factory.createFallback(Report.class), factory.createFallback(Report.class));
    }
}
