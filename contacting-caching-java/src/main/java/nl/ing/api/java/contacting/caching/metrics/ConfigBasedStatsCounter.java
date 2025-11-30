package nl.ing.api.java.contacting.caching.metrics;

import com.codahale.metrics.MetricRegistry;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.java.contacting.caching.core.StatsCounterProvider;
import java.time.Duration;

import static nl.ing.api.java.contacting.caching.util.ConfigConversions.*;
import static org.apache.commons.lang3.SystemUtils.getHostName;

/**
 * Provides a StatsCounter based on configuration.
 */
@Slf4j
public final class ConfigBasedStatsCounter implements StatsCounterProvider {

    private final StatsCounter statsCounter;

    @Override
    public StatsCounter statsCounter() {
        return statsCounter;
    }

    public ConfigBasedStatsCounter(Config config, String metricConfigPrefix) {
        StatsCounter counter;
        boolean metricsEnabled = getDefaultBoolean(config, metricConfigPrefix + ".metrics-enabled", false);

        if (metricsEnabled) {
            try {
                Config graphiteConfig = config.getConfig("graphite");
                String hostName = getHostName();
                String rootPrefix = graphiteConfig.getString("prefix") + "." + hostName;
                String metricsPrefix = getDefaultString(config, metricConfigPrefix + ".metrics-prefix", metricConfigPrefix);
                Duration metricsInterval = getDefaultDuration(config, metricConfigPrefix + ".metrics-interval", Duration.ofMillis(60000L));

                MetricRegistry metricRegistry = new MetricRegistry();

                MetricsStatsConfig metricsStatsConfig = new MetricsStatsConfig(
                        rootPrefix,
                        metricsPrefix,
                        graphiteConfig.getString("host"),
                        graphiteConfig.getInt("port"),
                        metricsInterval
                );

                GraphiteUtil.startGraphiteReporter(metricRegistry, metricsStatsConfig);

                counter = new DefaultCacheMetricsStatsCounter(metricsPrefix, metricRegistry);
            } catch (Exception e) {
                log.warn("Failed to start Graphite reporter, using NoopStatsCounterJava", e);
                counter = new NoopStatsCounter();
            }
        } else {
            counter = new NoopStatsCounter();
        }
        this.statsCounter = counter;
    }

}
