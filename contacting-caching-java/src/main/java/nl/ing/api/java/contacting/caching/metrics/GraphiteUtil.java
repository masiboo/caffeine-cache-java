package nl.ing.api.java.contacting.caching.metrics;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;

import java.util.concurrent.TimeUnit;

/**
 * Utility for starting a Graphite metrics reporter.
 */
public final class GraphiteUtil {

    private GraphiteUtil() {
        // Utility class; prevent instantiation
    }

    public static void startGraphiteReporter(MetricRegistry registry, MetricsStatsConfig metricConfig) {
        Graphite graphite = new Graphite(metricConfig.graphiteHost(), metricConfig.graphitePort());

        GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
            .prefixedWith(metricConfig.rootPrefix())
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .build(graphite);

        reporter.start(metricConfig.metricsInterval().toMillis(), TimeUnit.MILLISECONDS);
    }
}
