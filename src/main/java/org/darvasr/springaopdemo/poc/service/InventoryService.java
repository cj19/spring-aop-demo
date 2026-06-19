package org.darvasr.springaopdemo.poc.service;

import org.darvasr.springaopdemo.model.StockLevel;
import org.darvasr.springaopdemo.poc.exception.OutOfStockException;
import org.darvasr.springaopdemo.poc.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * POC inventory service without local handling.
 */
@Service("pocInventoryService")
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    public void reserve(String sku, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Invalid reservation quantity: " + quantity);
        }
        log.info("Reserved {} units of: {}", quantity, sku);
    }

    public void release(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new ValidationException("Invalid SKU for release");
        }
        log.info("Released: {}", sku);
    }

    public StockLevel checkStock(String sku) {
        if (sku == null || sku.startsWith("unknown")) {
            throw new OutOfStockException("Unknown or out-of-stock SKU: " + sku);
        }
        return new StockLevel(sku, 42, false);
    }
}
