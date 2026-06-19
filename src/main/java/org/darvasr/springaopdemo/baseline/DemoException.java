package org.darvasr.springaopdemo.baseline;

/**
 * The single shared exception type of the baseline branch. Every baseline service
 * throws this and handles it locally in a {@code try/catch} block — the existing,
 * repetitive code style that the POC branch replaces with AOP.
 */
public class DemoException extends RuntimeException {

    public DemoException(String message) {
        super(message);
    }
}
