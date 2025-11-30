package nl.ing.api.java.contacting.caching.util;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Utility class for cache operations.
 */
@Slf4j
public final class CacheOps {

    private CacheOps() {
        // Utility class; prevent instantiation
    }

    public record Flags(boolean skipCache) {

        public static final Flags DEFAULT = new Flags(false);

    }

    public static <K, V> CompletableFuture<V> fromCache(
            K key,
            Function<K, CompletableFuture<V>> loader,
            Cache<K, V> cache,
            Flags flags
    ) {
        if (flags.skipCache()) {
            return retrieveAndStore(key, loader, cache);
        }
        Optional<V> cachedValue = Optional.ofNullable(cache.getIfPresent(key));
        return cachedValue.map(CompletableFuture::completedFuture).orElseGet(() -> retrieveAndStore(key, loader, cache));
    }

    public static <K, V> CompletableFuture<V> fromAsyncLoadingCache(
            K key,
            Function<K, CompletableFuture<V>> loader,
            AsyncLoadingCache<K, V> cache,
            Flags flags
    ) {
        if (flags.skipCache()) {
            return retrieveAndStoreInAsyncCache(key, loader, cache);
        }
        return  Optional.ofNullable(cache.getIfPresent(key))
                .orElseGet(() -> retrieveAndStoreInAsyncCache(key, loader, cache));
    }

    public static <K, V> CompletableFuture<V> fallBack(
            K key,
            Function<K, CompletableFuture<V>> loader,
            Cache<K, V> cache
    ) {
        return loader.apply(key).handle((result, error) -> {
            if (error == null) {
                storeInCache(key, result, cache);
                return result;
            }
            log.warn("Fetching data from cache because of error in underlying service", error);
            V cachedValue = cache.getIfPresent(key);
            if (cachedValue == null) {
                log.error("Cache miss during fallBack for key {}", key);
                throw new RuntimeException(error);
            }
            return cachedValue;
        });
    }

    private static <K, V> CompletableFuture<V> retrieveAndStore(
            K key,
            Function<K, CompletableFuture<V>> loader,
            Cache<K, V> cache
    ) {
        return loader.apply(key).thenApply(value -> { storeInCache(key, value, cache); return value; });
    }

    private static <K, V> CompletableFuture<V> retrieveAndStoreInAsyncCache(
            K key,
            Function<K, CompletableFuture<V>> loader,
            AsyncLoadingCache<K, V> cache
    ) {
        return loader.apply(key).thenApply(value -> { storeInAsyncCache(key, value, cache); return value; });
    }

    private static <K, V> void storeInCache(K key, V value, Cache<K, V> cache) {
        try {
            cache.put(key, value);
        } catch (Exception e) {
            log.warn("Failed to write to cache. Key = {}", key, e);
        }
    }

    private static <K, V> void storeInAsyncCache(K key, V value, AsyncLoadingCache<K, V> cache) {
        try {
            cache.put(key,CompletableFuture.completedFuture(value));
        } catch (Exception e) {
            log.warn("Failed to write to cache. Key = {}", key, e);
        }
    }
}
