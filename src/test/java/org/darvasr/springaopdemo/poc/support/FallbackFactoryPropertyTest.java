package org.darvasr.springaopdemo.poc.support;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.darvasr.springaopdemo.model.Order;
import org.darvasr.springaopdemo.model.Receipt;
import org.darvasr.springaopdemo.model.Report;
import org.darvasr.springaopdemo.model.StockLevel;
import org.darvasr.springaopdemo.model.Summary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based tests for {@link FallbackFactory} (Req 6.2, 8.2).
 */
class FallbackFactoryPropertyTest {

    private final FallbackFactory factory = new FallbackFactory();

    @Property
    void fallbackIsAlwaysAssignableToRequestedType(@ForAll("registeredTypes") Class<?> type) {
        Object result = factory.createFallback(type);
        assertNotNull(result);
        assertTrue(type.isInstance(result), "Fallback is not assignment-compatible: " + type);
    }

    @Property
    void fallbackIsDeterministic(@ForAll("registeredTypes") Class<?> type) {
        assertEquals(factory.createFallback(type), factory.createFallback(type));
    }

    @Provide
    Arbitrary<Class<?>> registeredTypes() {
        return Arbitraries.of(Order.class, Receipt.class, StockLevel.class, Report.class, Summary.class);
    }
}
