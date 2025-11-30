package nl.ing.api.java.contacting.caching.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.benmanes.caffeine.cache.RemovalCause;

/**
 * Provides a cache removal listener that logs evictions using SLF4J.
 */
@Slf4j
public final class RemovalListener {

    @FunctionalInterface
    public interface RemovalListenerFunc<K, V> {
        void onRemoval(K key, V value, RemovalCause cause);
    }

    public static <K, V> RemovalListenerFunc<K, V> logRemovalListener() {
        return (key, value, cause) -> log.debug("Cache evicted for {}, cause: {}", key, cause);
    }

    private RemovalListener() {
        // Utility class; prevent instantiation
    }
}