package org.darvasr.springaopdemo.poc.exception;

/**
 * Common abstract base class of the POC branch exceptions. Every specific POC
 * exception derives from this, so the aspect can intercept them all with a single
 * pointcut while the concrete type still carries the error-case-specific meaning.
 */
public abstract class PocException extends RuntimeException {

    protected PocException(String message) {
        super(message);
    }
}
