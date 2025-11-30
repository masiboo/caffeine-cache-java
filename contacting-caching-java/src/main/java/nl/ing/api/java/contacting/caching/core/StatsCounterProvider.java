package nl.ing.api.java.contacting.caching.core;

import com.github.benmanes.caffeine.cache.stats.StatsCounter;

/**
 * Provides a StatsCounter instance.
 */
public interface StatsCounterProvider {

    StatsCounter statsCounter();

}
