package org.darvasr.springaopdemo.baseline;

import org.darvasr.springaopdemo.model.StockLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Baseline inventory service with local {@code try/catch} handling.
 */
@Service("baselineInventoryService")
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    public void reserve(String sku, int quantity) {
        try {
            if (quantity <= 0) {
                throw new DemoException("Invalid reservation quantity: " + quantity);
            }
            log.info("Reserved {} units of: {}", quantity, sku);
        } catch (DemoException ex) {
            log.error("Error during reserve", ex);
        }
    }

    public void release(String sku) {
        try {
            if (sku == null || sku.isBlank()) {
                throw new DemoException("Invalid SKU for release");
            }
            log.info("Released: {}", sku);
        } catch (DemoException ex) {
            log.error("Error during release", ex);
        }
    }

    public StockLevel checkStock(String sku) {
        try {
            if (sku == null || sku.startsWith("unknown")) {
                throw new DemoException("Unknown SKU: " + sku);
            }
            return new StockLevel(sku, 42, false);
        } catch (DemoException ex) {
            log.warn("Error during checkStock, returning fallback", ex);
            return new StockLevel(sku, 0, true);
        }
    }
}
