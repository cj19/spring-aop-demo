package org.darvasr.springaopdemo;

import org.darvasr.springaopdemo.model.Order;
import org.darvasr.springaopdemo.model.Receipt;
import org.darvasr.springaopdemo.model.StockLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behavioral equivalence: the baseline (local try/catch) and the POC (annotation +
 * AOP) branches behave observably the same from the caller's perspective
 * (Req 11.2, 11.3).
 */
@SpringBootTest
class EquivalenceTest {

    @Autowired
    private org.darvasr.springaopdemo.baseline.OrderService baselineOrders;
    @Autowired
    private org.darvasr.springaopdemo.baseline.PaymentService baselinePayments;
    @Autowired
    private org.darvasr.springaopdemo.baseline.InventoryService baselineInventory;
    @Autowired
    private org.darvasr.springaopdemo.baseline.NotificationService baselineNotifications;

    @Autowired
    private org.darvasr.springaopdemo.poc.service.OrderService pocOrders;
    @Autowired
    private org.darvasr.springaopdemo.poc.service.PaymentService pocPayments;
    @Autowired
    private org.darvasr.springaopdemo.poc.service.InventoryService pocInventory;
    @Autowired
    private org.darvasr.springaopdemo.poc.service.NotificationService pocNotifications;

    @Test
    void valueReturningMethodsAreEquivalentOnError() {
        Order baseOrder = baselineOrders.findOrder("missing-1");
        Order pocOrder = pocOrders.findOrder("missing-1");
        assertTrue(baseOrder.fallback());
        assertEquals(baseOrder.fallback(), pocOrder.fallback());
        assertEquals(baseOrder.getClass(), pocOrder.getClass());

        Receipt baseReceipt = baselinePayments.charge("acc", BigDecimal.valueOf(-1));
        Receipt pocReceipt = pocPayments.charge("acc", BigDecimal.valueOf(-1));
        assertTrue(baseReceipt.fallback());
        assertEquals(baseReceipt.fallback(), pocReceipt.fallback());

        StockLevel baseStock = baselineInventory.checkStock("unknown-1");
        StockLevel pocStock = pocInventory.checkStock("unknown-1");
        assertTrue(baseStock.fallback());
        assertEquals(baseStock.fallback(), pocStock.fallback());
    }

    @Test
    void voidMethodsAreEquivalentOnError() {
        assertDoesNotThrow(() -> baselineOrders.cancelOrder("locked-1"));
        assertDoesNotThrow(() -> pocOrders.cancelOrder("locked-1"));

        assertDoesNotThrow(() -> baselineNotifications.sendEmail("bad-address"));
        assertDoesNotThrow(() -> pocNotifications.sendEmail("bad-address"));
    }
}
