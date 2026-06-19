package org.darvasr.springaopdemo.model;

/**
 * Stock level domain model. The {@code fallback} field marks whether the instance
 * is a fallback value (true) or a real business result (false).
 */
public record StockLevel(String sku, int quantity, boolean fallback) {
}
