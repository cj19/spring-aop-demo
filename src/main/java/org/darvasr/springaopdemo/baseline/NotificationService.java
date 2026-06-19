package org.darvasr.springaopdemo.baseline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Baseline notification service with local {@code try/catch} handling.
 */
@Service("baselineNotificationService")
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendEmail(String to) {
        try {
            if (to == null || !to.contains("@")) {
                throw new DemoException("Invalid email address: " + to);
            }
            log.info("Email sent to: {}", to);
        } catch (DemoException ex) {
            log.error("Error during sendEmail", ex);
        }
    }
}
