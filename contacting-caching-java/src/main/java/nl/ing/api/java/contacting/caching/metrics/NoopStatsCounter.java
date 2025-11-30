package nl.ing.api.java.contacting.caching.metrics;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;

/**
 * No-op implementation of StatsCounter for Caffeine cache metrics.
 */
public final class NoopStatsCounter implements StatsCounter {

    // No need for singleton; allow instantiation as needed
    public NoopStatsCounter() {
        // No-op constructor
    }

    @Override
    public void recordHits(int count) {
        // No-op
    }

    @Override
    public void recordMisses(int count) {
        // No-op
    }

    @Override
    public void recordLoadSuccess(long loadTime) {
        // No-op
    }

    @Override
    public void recordLoadFailure(long loadTime) {
        // No-op
    }

    @Override
    public CacheStats snapshot() {
        return CacheStats.empty();
    }

    @Override
    public String toString() {
        return snapshot().toString();
    }

    @Override
    public void recordEviction(int count, RemovalCause cause) {
        // No-op
    }
}
