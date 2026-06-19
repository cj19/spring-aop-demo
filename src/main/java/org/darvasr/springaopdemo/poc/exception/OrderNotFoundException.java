package org.darvasr.springaopdemo.poc.exception;

/** Signals a non-existent order. */
public class OrderNotFoundException extends PocException {

    public OrderNotFoundException(String message) {
        super(message);
    }
}
