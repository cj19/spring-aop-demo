package org.darvasr.springaopdemo.model;

import java.math.BigDecimal;

/**
 * Order domain model. The {@code fallback} field marks whether the instance is a
 * fallback value (true) or a real business result (false).
 */
public record Order(String id, String customer, BigDecimal total, boolean fallback) {
}
