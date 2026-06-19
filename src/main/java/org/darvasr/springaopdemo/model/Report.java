package org.darvasr.springaopdemo.model;

import java.util.List;

/**
 * Report domain model. The {@code fallback} field marks whether the instance is a
 * fallback value (true) or a real business result (false).
 */
public record Report(String name, List<String> rows, boolean fallback) {
}
