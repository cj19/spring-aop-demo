package org.darvasr.springaopdemo.poc.service;

import org.springframework.stereotype.Service;

/**
 * Test fixture in the package covered by the POC pointcut: it throws an exception
 * that has no {@code @Handles} handler, so the aspect must rethrow it unchanged
 * (selective handling, Property 7 / Req 10.1).
 */
@Service("unhandledThrowingService")
public class UnhandledThrowingService {

    public String doWork() {
        throw new NumberFormatException("There is no registered handler for this");
    }
}
