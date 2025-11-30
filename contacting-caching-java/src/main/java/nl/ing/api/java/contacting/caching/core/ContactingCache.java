package nl.ing.api.java.contacting.caching.core;

import com.github.benmanes.caffeine.cache.*;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.java.contacting.caching.models.CacheConfig;
import nl.ing.api.java.contacting.caching.util.CacheOps;
import nl.ing.api.java.contacting.caching.util.RemovalListener;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@Slf4j
public abstract class ContactingCache implements ConfigProvider {

    private static final CacheConfig DEFAULT_CONFIG =
            new CacheConfig("api-cache", Duration.ofSeconds(1), Duration.ofMillis(Long.MAX_VALUE), 1000, false);

    private final ConcurrentMap<String, CacheConfig> configs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AsyncLoadingCache<Object, Object>> asyncCaches = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LoadingCache<Object, Object>> caches = new ConcurrentHashMap<>();

    // ----------------- SYNC CACHE -----------------
    public <K, V> V fromCacheableSyncFunction(String cacheName, K key,
                                              Function<K, V> loader,
                                              RemovalListener.RemovalListenerFunc<K, V> removalListener,
                                              CacheOps.Flags flags) {
        LoadingCache<K, V> cache = getOrCreateSyncCache(cacheName, removalListener);
        if (!flags.skipCache()) {
            return cache.get(key, loader::apply);
        } else {
            V value = loader.apply(key);
            cache.put(key, value);
            return value;
        }
    }

    public <K, V> V fallBack(String cacheName, K key, Function<K, V> loader,
                             RemovalListener.RemovalListenerFunc<K, V> removalListener) {
        try {
            V value = loader.apply(key);
            getOrCreateSyncCache(cacheName, loader, removalListener).put(key, value);
            return value;
        } catch (Exception e) {
            log.error("Error running loader for cache {}, returning cached value", cacheName, e);
            return getOrCreateSyncCache(cacheName, loader, removalListener).get(key);
        }
    }

    private <K, V> LoadingCache<K, V> getOrCreateSyncCacheInternal(String cacheName,
                                                                 CacheLoader<K, V> loader,
                                                                 RemovalListener.RemovalListenerFunc<K, V> removalListener) {
        synchronized (caches) {
            @SuppressWarnings("{unchecked}")
            LoadingCache<K, V> cache = (LoadingCache<K, V>) caches.get(cacheName);
            if (cache == null) {
                LoadingCache<K, V> newCache = buildCache(getCacheConfig(cacheName), removalListener).build(loader);
                ContactingCacheMeterRegistry.getRegistries()
                        .forEach(registry -> CaffeineCacheMetrics.monitor(registry, newCache, getCacheConfig(cacheName).name(), new ArrayList<>()));
                caches.put(cacheName, (LoadingCache<Object, Object>) newCache);
                cache = newCache;
            }
            return cache;
        }
    }

    private <K, V> LoadingCache<K, V> getOrCreateSyncCache(String cacheName,
                                                           RemovalListener.RemovalListenerFunc<K, V> removalListener) {
        return getOrCreateSyncCacheInternal(cacheName, new CacheLoader<K, V>() {
            @Override
            public V load(K key) { return null; }
        }, removalListener);
    }

    private <K, V> LoadingCache<K, V> getOrCreateSyncCache(String cacheName,
                                                           Function<K, V> loader,
                                                           RemovalListener.RemovalListenerFunc<K, V> removalListener) {
        return getOrCreateSyncCacheInternal(cacheName, new CacheLoader<K, V>() {
            @Override
            public V load(K key) { return loader.apply(key); }
        }, removalListener);
    }

    // ----------------- ASYNC CACHE -----------------
    public <K, V> CompletableFuture<V> fromCacheableFunction(String cacheName, K key,
                                                             Function<K, CompletableFuture<V>> loader,
                                                             RemovalListener.RemovalListenerFunc<K, V> removalListener,
                                                             CacheOps.Flags flags) {
        AsyncLoadingCache<K, V> cache = getOrCreateAsyncCacheF(cacheName, loader, removalListener);
        if (!flags.skipCache()) {
            return cache.get(key);
        } else {
            return loader.apply(key).thenApply(value -> {
                cache.put(key, CompletableFuture.completedFuture(value));
                return value;
            });
        }
    }

    public <K, V> CompletableFuture<V> fallBackAsyncF(String cacheName, K key,
                                                      Function<K, CompletableFuture<V>> loader,
                                                      RemovalListener.RemovalListenerFunc<K, V> removalListener) {
        CompletableFuture<V> result = loader.apply(key);
        result.thenAccept(value -> getOrCreateAsyncCacheF(cacheName, loader, removalListener).put(key, result));
        return result.exceptionally(ex -> {
            log.error("Error executing loader for cache {}, returning cached data", cacheName, ex);
            return fromCacheableFunction(cacheName, key, loader, removalListener, CacheOps.Flags.DEFAULT).join();
        });
    }

    private <K, V> AsyncLoadingCache<K, V> getOrCreateAsyncCacheF(String cacheName,
                                                                  Function<K, CompletableFuture<V>> loader,
                                                                  RemovalListener.RemovalListenerFunc<K, V> removalListener) {
        synchronized (asyncCaches) {
            @SuppressWarnings("{unchecked}")
            AsyncLoadingCache<K, V> cache = (AsyncLoadingCache<K, V>) asyncCaches.get(cacheName);
            if (cache == null) {
                AsyncCacheLoader<K, V> asyncLoader = (key, executor) -> loader.apply(key);
                AsyncLoadingCache<K, V> newCache = buildCache(getCacheConfig(cacheName), removalListener).buildAsync(asyncLoader);
                ContactingCacheMeterRegistry.getRegistries()
                        .forEach(registry -> {
                            CaffeineCacheMetrics.monitor(registry, newCache.synchronous(),
                                    getCacheConfig(cacheName).name(), new ArrayList<>());
                        });
                asyncCaches.put(cacheName, (AsyncLoadingCache<Object, Object>) newCache);
                cache = newCache;
            }
            return cache;
        }
    }

    // ----------------- COMMON BUILD -----------------
    private <K, V> Caffeine<K, V> buildCache(CacheConfig config,
                                             RemovalListener.RemovalListenerFunc<K, V> removalListener) {
        return Caffeine.newBuilder()
                .expireAfterWrite(config.expireDuration())
                .refreshAfterWrite(config.refreshDuration())
                .maximumSize(config.maximumSize())
                .removalListener((K key, V value, RemovalCause cause) -> removalListener.onRemoval(key, value, cause))
                .recordStats()
                .executor(ContactingCacheExecutor.executor());
    }

    private CacheConfig getCacheConfig(String cacheName) {
        return configs.computeIfAbsent(cacheName,
                k -> CacheConfig.createFromConfig(cacheName, config()).orElse(DEFAULT_CONFIG));
    }

    // ----------------- CLEAR / INVALIDATE -----------------
    public Optional<Object> clearCache(String cacheName) {
        Object removed = caches.remove(cacheName);
        if (removed == null) removed = asyncCaches.remove(cacheName);
        return Optional.ofNullable(removed);
    }

    public Optional<Object> invalidateCache(String cacheName, Optional<Object> key) {
        if (key.isPresent()) {
            Object k = key.get();
            AsyncLoadingCache<Object, Object> asyncCache = asyncCaches.get(cacheName);
            if (asyncCache != null) asyncCache.synchronous().invalidate(k);
            LoadingCache<Object, Object> cache = caches.get(cacheName);
            if (cache != null) cache.invalidate(k);
            return Optional.of(k);
        } else {
            AsyncLoadingCache<Object, Object> asyncCache = asyncCaches.get(cacheName);
            if (asyncCache != null) asyncCache.synchronous().invalidateAll();
            LoadingCache<Object, Object> cache = caches.get(cacheName);
            if (cache != null) cache.invalidateAll();
            return Optional.of(cacheName);
        }
    }
}
