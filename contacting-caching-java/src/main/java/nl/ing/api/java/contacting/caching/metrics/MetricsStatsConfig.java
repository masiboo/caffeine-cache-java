package nl.ing.api.java.contacting.caching.metrics;

import java.time.Duration;

/**
 * Configuration for metrics and stats reporting.
 */
public record MetricsStatsConfig(
    String rootPrefix,
    String prefix,
    String graphiteHost,
    int graphitePort,
    Duration metricsInterval
) {}
