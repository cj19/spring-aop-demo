package org.darvasr.springaopdemo.baseline;

import org.darvasr.springaopdemo.model.Report;
import org.darvasr.springaopdemo.model.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Baseline report service with local {@code try/catch} handling.
 */
@Service("baselineReportService")
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    public Report generate(String name) {
        try {
            if (name == null || name.isBlank()) {
                throw new DemoException("Invalid report name");
            }
            return new Report(name, List.of("row-1", "row-2"), false);
        } catch (DemoException ex) {
            log.warn("Error during generate, returning fallback", ex);
            return new Report(name, List.of(), true);
        }
    }

    public Summary summarize(String name) {
        try {
            if (name == null || name.startsWith("empty")) {
                throw new DemoException("No data to summarize: " + name);
            }
            return new Summary(name, 2, false);
        } catch (DemoException ex) {
            log.warn("Error during summarize, returning fallback", ex);
            return new Summary(name, 0, true);
        }
    }
}
