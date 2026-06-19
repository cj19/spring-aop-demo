package org.darvasr.springaopdemo.model;

import java.math.BigDecimal;

/**
 * Payment receipt domain model. The {@code fallback} field marks whether the
 * instance is a fallback value (true) or a real business result (false).
 */
public record Receipt(String paymentId, BigDecimal amount, boolean fallback) {
}
