package nl.ing.api.java.contacting.caching.models;

import com.typesafe.config.Config;
import java.time.Duration;
import java.util.Optional;

/**
 * Case class representation for cache configuration.
 */
public record CacheConfig(
    String name,
    Duration refreshDuration,
    Duration expireDuration,
    long maximumSize,
    boolean customExecutor
) {
    /**
     * Creates an object representation of cache configuration from the given configuration.
     *
     * @param name   name of the cache
     * @param config configuration
     * @return Optional containing CacheConfig if config path exists, otherwise Optional.empty()
     */
    public static Optional<CacheConfig> createFromConfig(String name, Config config) {
        String cacheConfigPath = "caching." + name;
        if (config.hasPath(cacheConfigPath)) {
            Duration refreshDuration = config.getDuration(cacheConfigPath + ".refresh-duration");
            Duration expireDuration;
            try {
                expireDuration = config.getDuration(cacheConfigPath + ".expire-duration");
            } catch (Exception e) {
                expireDuration = Duration.ofSeconds(Long.MAX_VALUE); // Scala's Duration.Inf
            }
            long maxSize;
            try {
                maxSize = config.getLong(cacheConfigPath + ".max-size");
            } catch (Exception e) {
                maxSize = 1500;
            }
            boolean customExecutor;
            try {
                customExecutor = config.getBoolean(cacheConfigPath + ".custom-executor");
            } catch (Exception e) {
                customExecutor = false;
            }
            return Optional.of(new CacheConfig(name, refreshDuration, expireDuration, maxSize, customExecutor));
        } else {
            return Optional.empty();
        }
    }
}
