package org.darvasr.springaopdemo;

import org.darvasr.springaopdemo.model.Order;
import org.darvasr.springaopdemo.model.Receipt;
import org.darvasr.springaopdemo.model.Report;
import org.darvasr.springaopdemo.model.StockLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Runs the baseline (local try/catch) and the POC (annotation + AOP) branches side
 * by side for the same error cases, so the behavioral equivalence is visible from
 * the caller's perspective, along with the void, value and reactive handling modes.
 *
 * <p>Disabled by default. Enable the boot-time demo with
 * {@code demo.runner.enabled=true} (e.g. in application.properties or as a CLI arg).
 * The REST endpoints under {@code /api/**} are the primary way to exercise the
 * services.
 */
@Component
@ConditionalOnProperty(name = "demo.runner.enabled", havingValue = "true")
public class DemoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);

    private final org.darvasr.springaopdemo.baseline.OrderService baselineOrders;
    private final org.darvasr.springaopdemo.baseline.PaymentService baselinePayments;
    private final org.darvasr.springaopdemo.baseline.InventoryService baselineInventory;
    private final org.darvasr.springaopdemo.baseline.NotificationService baselineNotifications;

    private final org.darvasr.springaopdemo.poc.service.OrderService pocOrders;
    private final org.darvasr.springaopdemo.poc.service.PaymentService pocPayments;
    private final org.darvasr.springaopdemo.poc.service.InventoryService pocInventory;
    private final org.darvasr.springaopdemo.poc.service.NotificationService pocNotifications;
    private final org.darvasr.springaopdemo.poc.service.ReportService pocReports;

    public DemoRunner(org.darvasr.springaopdemo.baseline.OrderService baselineOrders,
                      org.darvasr.springaopdemo.baseline.PaymentService baselinePayments,
                      org.darvasr.springaopdemo.baseline.InventoryService baselineInventory,
                      org.darvasr.springaopdemo.baseline.NotificationService baselineNotifications,
                      org.darvasr.springaopdemo.poc.service.OrderService pocOrders,
                      org.darvasr.springaopdemo.poc.service.PaymentService pocPayments,
                      org.darvasr.springaopdemo.poc.service.InventoryService pocInventory,
                      org.darvasr.springaopdemo.poc.service.NotificationService pocNotifications,
                      org.darvasr.springaopdemo.poc.service.ReportService pocReports) {
        this.baselineOrders = baselineOrders;
        this.baselinePayments = baselinePayments;
        this.baselineInventory = baselineInventory;
        this.baselineNotifications = baselineNotifications;
        this.pocOrders = pocOrders;
        this.pocPayments = pocPayments;
        this.pocInventory = pocInventory;
        this.pocNotifications = pocNotifications;
        this.pocReports = pocReports;
    }

    @Override
    public void run(String... args) {
        log.info("===== BASELINE BRANCH (local try/catch) =====");
        Order bOrder = baselineOrders.findOrder("missing-1");
        log.info("baseline findOrder -> {}", bOrder);
        Receipt bReceipt = baselinePayments.charge("acc-1", BigDecimal.valueOf(-5));
        log.info("baseline charge -> {}", bReceipt);
        StockLevel bStock = baselineInventory.checkStock("unknown-9");
        log.info("baseline checkStock -> {}", bStock);
        baselineOrders.cancelOrder("locked-1");
        baselineNotifications.sendEmail("bad-address");

        log.info("===== POC BRANCH (annotation + AOP) =====");
        Order pOrder = pocOrders.findOrder("missing-1");
        log.info("poc findOrder -> {}", pOrder);
        Receipt pReceipt = pocPayments.charge("acc-1", BigDecimal.valueOf(-5));
        log.info("poc charge -> {}", pReceipt);
        StockLevel pStock = pocInventory.checkStock("unknown-9");
        log.info("poc checkStock -> {}", pStock);
        pocOrders.cancelOrder("locked-1");       // void: only logging
        pocNotifications.sendEmail("bad-address"); // void: only logging

        log.info("===== POC pass-through (no error) =====");
        log.info("poc createOrder -> {}", pocOrders.createOrder("Bela"));
        log.info("poc generate -> {}", pocReports.generate("Q3"));

        log.info("===== POC reactive case =====");
        Report reactiveFallback = pocReports.generateReactive("").block();
        log.info("poc generateReactive (error) -> {}", reactiveFallback);
        Report reactiveOk = pocReports.generateReactive("Q4").block();
        log.info("poc generateReactive (success) -> {}", reactiveOk);
    }
}
