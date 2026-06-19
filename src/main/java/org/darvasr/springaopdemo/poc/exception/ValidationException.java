package org.darvasr.springaopdemo.poc.exception;

/** Signals invalid input. */
public class ValidationException extends PocException {

    public ValidationException(String message) {
        super(message);
    }
}
