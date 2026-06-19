package org.darvasr.springaopdemo.poc.aspect;

import org.darvasr.springaopdemo.model.Order;
import org.darvasr.springaopdemo.model.Receipt;
import org.darvasr.springaopdemo.model.StockLevel;
import org.darvasr.springaopdemo.poc.service.InventoryService;
import org.darvasr.springaopdemo.poc.service.NotificationService;
import org.darvasr.springaopdemo.poc.service.OrderService;
import org.darvasr.springaopdemo.poc.service.PaymentService;
import org.darvasr.springaopdemo.poc.service.UnhandledThrowingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the behavior of the central {@link ExceptionHandlingAspect}
 * through proxied POC beans (Req 5.1, 5.6, 6.1, 6.2, 9.1, 9.2, 10.1).
 */
@SpringBootTest
class ExceptionHandlingAspectIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UnhandledThrowingService unhandledThrowingService;

    @Test
    void valueReturningMethodGetsFallbackOnException() {
        Order order = orderService.findOrder("missing-1");
        assertTrue(order.fallback(), "On error a fallback Order should be returned");
    }

    @Test
    void otherValueHandlersProduceTypedFallbacks() {
        Receipt receipt = paymentService.charge("acc", BigDecimal.valueOf(-1));
        assertTrue(receipt.fallback());

        StockLevel stock = inventoryService.checkStock("unknown-1");
        assertTrue(stock.fallback());
    }

    @Test
    void successfulCallPassesThroughUnchanged() {
        Order order = orderService.findOrder("ORD-9");
        assertFalse(order.fallback(), "Without an error the original result should be returned");
    }

    @Test
    void voidMethodExceptionIsSwallowed() {
        assertDoesNotThrow(() -> notificationService.sendEmail("bad-address"));
        assertDoesNotThrow(() -> orderService.cancelOrder("locked-1"));
    }

    @Test
    void unhandledExceptionIsRethrown() {
        assertThrows(NumberFormatException.class, () -> unhandledThrowingService.doWork());
    }
}
