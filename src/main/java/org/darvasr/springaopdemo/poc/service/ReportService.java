package org.darvasr.springaopdemo.poc.service;

import org.darvasr.springaopdemo.model.Report;
import org.darvasr.springaopdemo.model.Summary;
import org.darvasr.springaopdemo.poc.exception.ReportGenerationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * POC report service without local handling.
 *
 * <p>{@code generate} and {@code summarize} demonstrate the successful
 * (pass-through) case: the aspect forwards the result unchanged. {@code
 * generateReactive} demonstrates the reactive case: the {@code Mono} emits an error
 * that the central reactive handler replaces with a reactive fallback result.
 */
@Service("pocReportService")
public class ReportService {

    public Report generate(String name) {
        return new Report(name, List.of("row-1", "row-2"), false);
    }

    public Summary summarize(String name) {
        return new Summary(name, 2, false);
    }

    public Mono<Report> generateReactive(String name) {
        if (name == null || name.isBlank()) {
            return Mono.error(new ReportGenerationException("Reactive report generation failure: " + name));
        }
        return Mono.just(new Report(name, List.of("reactive-row-1"), false));
    }
}
