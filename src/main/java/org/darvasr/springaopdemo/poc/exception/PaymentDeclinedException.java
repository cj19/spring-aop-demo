package org.darvasr.springaopdemo.poc.exception;

/** Signals a declined payment. */
public class PaymentDeclinedException extends PocException {

    public PaymentDeclinedException(String message) {
        super(message);
    }
}
