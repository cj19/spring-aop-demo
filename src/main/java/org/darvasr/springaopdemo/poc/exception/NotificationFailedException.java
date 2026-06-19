package org.darvasr.springaopdemo.poc.exception;

/** Signals a notification delivery failure. */
public class NotificationFailedException extends PocException {

    public NotificationFailedException(String message) {
        super(message);
    }
}
