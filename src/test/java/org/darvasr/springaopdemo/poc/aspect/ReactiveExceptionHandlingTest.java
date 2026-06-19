package org.darvasr.springaopdemo.poc.aspect;

import org.darvasr.springaopdemo.poc.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the reactive branch: an error-emitting {@code Mono} is replaced by the
 * reactive handler's fallback result, without an error (Req 6.3, 7.2, 7.3).
 */
@SpringBootTest
class ReactiveExceptionHandlingTest {

    @Autowired
    private ReportService reportService;

    @Test
    void reactiveErrorIsReplacedByFallbackEmission() {
        StepVerifier.create(reportService.generateReactive(""))
                .assertNext(report -> assertTrue(report.fallback(), "On error a fallback Report should be emitted"))
                .verifyComplete();
    }

    @Test
    void reactiveSuccessPassesThroughUnchanged() {
        StepVerifier.create(reportService.generateReactive("Q4"))
                .assertNext(report -> assertFalse(report.fallback()))
                .verifyComplete();
    }
}
