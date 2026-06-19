package org.darvasr.springaopdemo.poc.service;

import org.darvasr.springaopdemo.model.Order;
import org.darvasr.springaopdemo.poc.exception.OrderNotFoundException;
import org.darvasr.springaopdemo.poc.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * POC order service. No local {@code try/catch}: the methods only throw the
 * appropriate {@link org.darvasr.springaopdemo.poc.exception.PocException} subtype,
 * and the central aspect performs the handling.
 */
@Service("pocOrderService")
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public Order createOrder(String customer) {
        if (customer == null || customer.isBlank()) {
            throw new OrderNotFoundException("Unknown customer while creating order: " + customer);
        }
        return new Order("ORD-1", customer, new BigDecimal("100.00"), false);
    }

    public Order findOrder(String id) {
        if (id == null || id.startsWith("missing")) {
            throw new OrderNotFoundException("Order not found: " + id);
        }
        return new Order(id, "Anna Kovacs", new BigDecimal("250.00"), false);
    }

    public void cancelOrder(String id) {
        if (id == null || id.startsWith("locked")) {
            throw new ValidationException("Order cannot be cancelled: " + id);
        }
        log.info("Order cancelled: {}", id);
    }
}
