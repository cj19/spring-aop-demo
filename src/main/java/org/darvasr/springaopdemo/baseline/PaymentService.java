package org.darvasr.springaopdemo.baseline;

import org.darvasr.springaopdemo.model.Receipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Baseline payment service with local {@code try/catch} handling.
 */
@Service("baselinePaymentService")
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    public Receipt charge(String account, BigDecimal amount) {
        try {
            if (amount == null || amount.signum() <= 0) {
                throw new DemoException("Invalid payment amount: " + amount);
            }
            return new Receipt("PAY-1", amount, false);
        } catch (DemoException ex) {
            log.warn("Error during charge, returning fallback", ex);
            return new Receipt("PAY-0", BigDecimal.ZERO, true);
        }
    }

    public void refund(String paymentId) {
        try {
            if (paymentId == null || paymentId.startsWith("settled")) {
                throw new DemoException("Payment cannot be refunded: " + paymentId);
            }
            log.info("Refund completed: {}", paymentId);
        } catch (DemoException ex) {
            log.error("Error during refund", ex);
        }
    }
}
