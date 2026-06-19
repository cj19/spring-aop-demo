package org.darvasr.springaopdemo.poc.service;

import org.darvasr.springaopdemo.poc.exception.NotificationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * POC notification service without local handling. {@code sendEmail} is the
 * canonical void case: on error the central void handler only logs.
 */
@Service("pocNotificationService")
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendEmail(String to) {
        if (to == null || !to.contains("@")) {
            throw new NotificationFailedException("Invalid email address: " + to);
        }
        log.info("Email sent to: {}", to);
    }
}
