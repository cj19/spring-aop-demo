package org.darvasr.springaopdemo.web;

import org.darvasr.springaopdemo.baseline.InventoryService;
import org.darvasr.springaopdemo.baseline.NotificationService;
import org.darvasr.springaopdemo.baseline.OrderService;
import org.darvasr.springaopdemo.baseline.PaymentService;
import org.darvasr.springaopdemo.baseline.ReportService;
import org.darvasr.springaopdemo.model.Order;
import org.darvasr.springaopdemo.model.Receipt;
import org.darvasr.springaopdemo.model.Report;
import org.darvasr.springaopdemo.model.StockLevel;
import org.darvasr.springaopdemo.model.Summary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST endpoints for the baseline services (local try/catch). Mirrors
 * {@link PocController} so the two branches can be compared over HTTP for the same
 * trigger inputs.
 */
@RestController
@RequestMapping("/api/baseline")
public class BaselineController {

    private final OrderService orders;
    private final PaymentService payments;
    private final InventoryService inventory;
    private final NotificationService notifications;
    private final ReportService reports;

    public BaselineController(OrderService orders, PaymentService payments, InventoryService inventory,
                              NotificationService notifications, ReportService reports) {
        this.orders = orders;
        this.payments = payments;
        this.inventory = inventory;
        this.notifications = notifications;
        this.reports = reports;
    }

    @GetMapping("/orders/{id}")
    public Order findOrder(@PathVariable String id) {
        return orders.findOrder(id);
    }

    @GetMapping("/orders/create")
    public Order createOrder(@RequestParam(defaultValue = "") String customer) {
        return orders.createOrder(customer);
    }

    @GetMapping("/orders/{id}/cancel")
    public Map<String, String> cancelOrder(@PathVariable String id) {
        orders.cancelOrder(id);
        return Map.of("status", "ok", "operation", "cancelOrder", "note", "void method completed; check logs");
    }

    @GetMapping("/payments/charge")
    public Receipt charge(@RequestParam(defaultValue = "acc-1") String account,
                          @RequestParam(defaultValue = "100") BigDecimal amount) {
        return payments.charge(account, amount);
    }

    @GetMapping("/payments/refund")
    public Map<String, String> refund(@RequestParam(defaultValue = "PAY-1") String paymentId) {
        payments.refund(paymentId);
        return Map.of("status", "ok", "operation", "refund", "note", "void method completed; check logs");
    }

    @GetMapping("/inventory/reserve")
    public Map<String, String> reserve(@RequestParam(defaultValue = "SKU-1") String sku,
                                       @RequestParam(defaultValue = "1") int quantity) {
        inventory.reserve(sku, quantity);
        return Map.of("status", "ok", "operation", "reserve", "note", "void method completed; check logs");
    }

    @GetMapping("/inventory/release")
    public Map<String, String> release(@RequestParam(defaultValue = "SKU-1") String sku) {
        inventory.release(sku);
        return Map.of("status", "ok", "operation", "release", "note", "void method completed; check logs");
    }

    @GetMapping("/inventory/stock/{sku}")
    public StockLevel checkStock(@PathVariable String sku) {
        return inventory.checkStock(sku);
    }

    @GetMapping("/notifications/email")
    public Map<String, String> sendEmail(@RequestParam(defaultValue = "user@example.com") String to) {
        notifications.sendEmail(to);
        return Map.of("status", "ok", "operation", "sendEmail", "note", "void method completed; check logs");
    }

    @GetMapping("/reports/generate")
    public Report generate(@RequestParam(defaultValue = "Q3") String name) {
        return reports.generate(name);
    }

    @GetMapping("/reports/summarize")
    public Summary summarize(@RequestParam(defaultValue = "Q3") String name) {
        return reports.summarize(name);
    }
}
