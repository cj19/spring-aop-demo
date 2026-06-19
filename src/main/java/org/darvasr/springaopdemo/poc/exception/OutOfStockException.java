package org.darvasr.springaopdemo.poc.exception;

/** Signals an out-of-stock situation. */
public class OutOfStockException extends PocException {

    public OutOfStockException(String message) {
        super(message);
    }
}
