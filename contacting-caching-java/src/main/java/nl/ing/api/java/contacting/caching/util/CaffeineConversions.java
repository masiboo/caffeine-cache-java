package nl.ing.api.java.contacting.caching.util;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

/**
 * Utility class for safe Caffeine expiration and refresh operations.
 */
public final class CaffeineConversions {

    private CaffeineConversions() {
        // Utility class; prevent instantiation
    }

    /**
     * Applies expireAfterWrite only if the duration is not infinite, negative, or zero.
     */
    public static <K, V> Caffeine<K, V> expireAfterWriteOptionally(Caffeine<K, V> caffeine, Duration duration) {
        if (!duration.isNegative() && !duration.isZero() && !duration.equals(Duration.ofMillis(Long.MAX_VALUE))) {
            caffeine.expireAfterWrite(duration);
        }
        return caffeine;
    }

    /**
     * Applies refreshAfterWrite only if the duration is not zero.
     */
    public static <K, V> Caffeine<K, V> refreshAfterWriteOptionally(Caffeine<K, V> caffeine, Duration duration) {
        if (!duration.isZero()) {
            caffeine.refreshAfterWrite(duration);
        }
        return caffeine;
    }
}
