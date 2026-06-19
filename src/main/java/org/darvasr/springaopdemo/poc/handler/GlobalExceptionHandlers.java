package org.darvasr.springaopdemo.poc.handler;

import org.darvasr.springaopdemo.model.Order;
import org.darvasr.springaopdemo.model.Receipt;
import org.darvasr.springaopdemo.model.Report;
import org.darvasr.springaopdemo.model.StockLevel;
import org.darvasr.springaopdemo.poc.annotation.GlobalExceptionHandler;
import org.darvasr.springaopdemo.poc.annotation.Handles;
import org.darvasr.springaopdemo.poc.exception.NotificationFailedException;
import org.darvasr.springaopdemo.poc.exception.OrderNotFoundException;
import org.darvasr.springaopdemo.poc.exception.OutOfStockException;
import org.darvasr.springaopdemo.poc.exception.PaymentDeclinedException;
import org.darvasr.springaopdemo.poc.exception.ReportGenerationException;
import org.darvasr.springaopdemo.poc.exception.ValidationException;
import org.darvasr.springaopdemo.poc.support.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * The central handler methods in a single place, with an example for each of the
 * three handling modes:
 * <ul>
 *   <li><b>void</b> handler — only logs (in reality this could send e.g. a Kafka ack);</li>
 *   <li><b>value</b> handler — returns a fallback object compatible with the original return type;</li>
 *   <li><b>reactive</b> handler — returns a {@code Mono} fallback result.</li>
 * </ul>
 *
 * <p>The bean is CDI/Spring-aware: it injects {@link FallbackFactory} via the
 * constructor. Each {@link Handles} annotated method handles exactly one exception
 * type, and its return type is consistent with the return type of the service
 * methods that throw that exception.
 */
@GlobalExceptionHandler
public class GlobalExceptionHandlers {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandlers.class);

    private final FallbackFactory fallback;

    public GlobalExceptionHandlers(FallbackFactory fallback) {
        this.fallback = fallback;
    }

    // 1) VOID case: only logging (in reality e.g. a Kafka ack)
    @Handles(NotificationFailedException.class)
    public void onNotificationFailed(NotificationFailedException ex) {
        log.error("Notification failure handled (void): {}", ex.getMessage());
    }

    // 1b) VOID case for another exception type (precondition/validation failures from void methods)
    @Handles(ValidationException.class)
    public void onValidation(ValidationException ex) {
        log.error("Validation failure handled (void): {}", ex.getMessage());
    }

    // 2) VALUE case: fallback Order
    @Handles(OrderNotFoundException.class)
    public Order onOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found, fallback Order: {}", ex.getMessage());
        return fallback.createFallback(Order.class);
    }

    // 2b) VALUE case: fallback Receipt
    @Handles(PaymentDeclinedException.class)
    public Receipt onPaymentDeclined(PaymentDeclinedException ex) {
        log.warn("Payment declined, fallback Receipt: {}", ex.getMessage());
        return fallback.createFallback(Receipt.class);
    }

    // 2c) VALUE case: fallback StockLevel
    @Handles(OutOfStockException.class)
    public StockLevel onOutOfStock(OutOfStockException ex) {
        log.warn("Out of stock, fallback StockLevel: {}", ex.getMessage());
        return fallback.createFallback(StockLevel.class);
    }

    // 3) REACTIVE case: reactive fallback Report
    @Handles(ReportGenerationException.class)
    public Mono<Report> onReportFailedReactive(ReportGenerationException ex) {
        log.warn("Report generation failure, reactive fallback Report: {}", ex.getMessage());
        return Mono.just(fallback.createFallback(Report.class));
    }
}
