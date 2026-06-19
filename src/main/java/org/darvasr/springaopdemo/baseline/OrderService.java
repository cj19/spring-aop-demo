package org.darvasr.springaopdemo.baseline;

import org.darvasr.springaopdemo.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Baseline order service. Every method handles {@link DemoException} locally in a
 * {@code try/catch} block: value-returning methods return a hand-built fallback
 * object, the void method only logs.
 */
@Service("baselineOrderService")
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public Order createOrder(String customer) {
        try {
            if (customer == null || customer.isBlank()) {
                throw new DemoException("Invalid customer while creating order");
            }
            return new Order("ORD-1", customer, new BigDecimal("100.00"), false);
        } catch (DemoException ex) {
            log.warn("Error during createOrder, returning fallback", ex);
            return new Order("ORD-0", "UNKNOWN", BigDecimal.ZERO, true);
        }
    }

    public Order findOrder(String id) {
        try {
            if (id == null || id.startsWith("missing")) {
                throw new DemoException("Order not found: " + id);
            }
            return new Order(id, "Anna Kovacs", new BigDecimal("250.00"), false);
        } catch (DemoException ex) {
            log.warn("Error during findOrder, returning fallback", ex);
            return new Order(id, "UNKNOWN", BigDecimal.ZERO, true);
        }
    }

    public void cancelOrder(String id) {
        try {
            if (id == null || id.startsWith("locked")) {
                throw new DemoException("Order cannot be cancelled: " + id);
            }
            log.info("Order cancelled: {}", id);
        } catch (DemoException ex) {
            log.error("Error during cancelOrder", ex);
        }
    }
}
