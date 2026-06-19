package org.darvasr.springaopdemo.poc.support;

import org.darvasr.springaopdemo.model.Order;
import org.darvasr.springaopdemo.model.Receipt;
import org.darvasr.springaopdemo.model.Report;
import org.darvasr.springaopdemo.model.StockLevel;
import org.darvasr.springaopdemo.model.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Produces a deterministic "fallback" object for a given return type. Handler
 * methods can use it to build their fallback results uniformly.
 *
 * <p>The function is pure (side-effect free) and deterministic: for the same type it
 * always returns a structurally identical instance marked {@code fallback == true}.
 */
@Component
public class FallbackFactory {

    private static final Logger log = LoggerFactory.getLogger(FallbackFactory.class);

    private final Map<Class<?>, Supplier<?>> registry = Map.of(
            Order.class, () -> new Order("FALLBACK", "UNKNOWN", BigDecimal.ZERO, true),
            Receipt.class, () -> new Receipt("FALLBACK", BigDecimal.ZERO, true),
            StockLevel.class, () -> new StockLevel("FALLBACK", 0, true),
            Report.class, () -> new Report("FALLBACK", List.of(), true),
            Summary.class, () -> new Summary("FALLBACK", 0, true)
    );

    /**
     * Returns a fallback instance for the requested type.
     *
     * @param targetType the desired return type (not {@code null})
     * @return for a registered type a non-null instance marked {@code fallback == true};
     * for {@code void}/{@code Void} or an unregistered reference type {@code null};
     * for a primitive type the default value of that type
     */
    @SuppressWarnings("unchecked")
    public <T> T createFallback(Class<T> targetType) {
        if (targetType == null || targetType == void.class || targetType == Void.class) {
            return null;
        }

        Supplier<?> supplier = registry.get(targetType);
        if (supplier != null) {
            return (T) supplier.get();
        }

        if (targetType.isPrimitive()) {
            return (T) defaultPrimitive(targetType);
        }

        log.warn("No registered fallback for type: {}", targetType.getName());
        return null;
    }

    private Object defaultPrimitive(Class<?> primitive) {
        if (primitive == boolean.class) {
            return Boolean.FALSE;
        }
        if (primitive == char.class) {
            return '\0';
        }
        if (primitive == byte.class) {
            return (byte) 0;
        }
        if (primitive == short.class) {
            return (short) 0;
        }
        if (primitive == int.class) {
            return 0;
        }
        if (primitive == long.class) {
            return 0L;
        }
        if (primitive == float.class) {
            return 0f;
        }
        if (primitive == double.class) {
            return 0d;
        }
        return null;
    }
}
