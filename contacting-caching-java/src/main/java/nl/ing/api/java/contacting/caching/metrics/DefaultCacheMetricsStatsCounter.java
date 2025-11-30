package nl.ing.api.java.contacting.caching.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;

import java.util.concurrent.TimeUnit;

/**
 * Tracks cache metrics using Dropwizard MetricRegistry.
 */
public class DefaultCacheMetricsStatsCounter implements StatsCounter {

    private final Meter hitCount;
    private final Meter missCount;
    private final Timer totalLoadTime;
    private final Meter loadSuccessCount;
    private final Meter loadFailureCount;
    private final Meter evictionCount;
    private final Meter evictionWeight;

    public DefaultCacheMetricsStatsCounter(String cachePrefix, MetricRegistry metricRegistry) {
        this.hitCount = metricRegistry.meter(cachePrefix + ".hits");
        this.missCount = metricRegistry.meter(cachePrefix + ".misses");
        this.totalLoadTime = metricRegistry.timer(cachePrefix + ".loads");
        this.loadSuccessCount = metricRegistry.meter(cachePrefix + ".loads-success");
        this.loadFailureCount = metricRegistry.meter(cachePrefix + ".loads-failure");
        this.evictionCount = metricRegistry.meter(cachePrefix + ".evictions");
        this.evictionWeight = metricRegistry.meter(cachePrefix + ".evictions-weight");
    }

    @Override
    public void recordHits(int count) {
        hitCount.mark(count);
    }

    @Override
    public void recordMisses(int count) {
        missCount.mark(count);
    }

    @Override
    public void recordLoadSuccess(long loadTime) {
        loadSuccessCount.mark();
        totalLoadTime.update(loadTime, TimeUnit.NANOSECONDS);
    }

    @Override
    public void recordLoadFailure(long loadTime) {
        loadFailureCount.mark();
        totalLoadTime.update(loadTime, TimeUnit.NANOSECONDS);
    }

    @Override
    public void recordEviction(int count, RemovalCause removalCause) {
        evictionCount.mark(count);
        // Optionally, track eviction weight if available
    }

    @Override
    public CacheStats snapshot() {
        return CacheStats.of(
            hitCount.getCount(),
            missCount.getCount(),
            loadSuccessCount.getCount(),
            loadFailureCount.getCount(),
            totalLoadTime.getCount(),
            evictionCount.getCount(),
            evictionWeight.getCount()
        );
    }

    @Override
    public String toString() {
        return snapshot().toString();
    }
}
