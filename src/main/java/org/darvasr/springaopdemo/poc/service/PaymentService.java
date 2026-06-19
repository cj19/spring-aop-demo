package org.darvasr.springaopdemo.poc.service;

import org.darvasr.springaopdemo.model.Receipt;
import org.darvasr.springaopdemo.poc.exception.PaymentDeclinedException;
import org.darvasr.springaopdemo.poc.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * POC payment service without local handling.
 */
@Service("pocPaymentService")
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    public Receipt charge(String account, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new PaymentDeclinedException("Invalid payment amount: " + amount);
        }
        return new Receipt("PAY-1", amount, false);
    }

    public void refund(String paymentId) {
        if (paymentId == null || paymentId.startsWith("settled")) {
            throw new ValidationException("Payment cannot be refunded: " + paymentId);
        }
        log.info("Refund completed: {}", paymentId);
    }
}
