package org.darvasr.springaopdemo.poc.exception;

/** Signals a report generation failure. */
public class ReportGenerationException extends PocException {

    public ReportGenerationException(String message) {
        super(message);
    }
}
