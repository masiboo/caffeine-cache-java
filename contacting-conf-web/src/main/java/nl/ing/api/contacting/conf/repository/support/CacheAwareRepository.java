package nl.ing.api.contacting.conf.repository.support;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.java.contacting.caching.core.ContactingCache;
import nl.ing.api.java.contacting.caching.util.CacheOps;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public abstract class CacheAwareRepository<T, ID> {

    private ContactingCache contactingCache;

    public CacheAwareRepository(ContactingCache contactingCache) {
        this.contactingCache = contactingCache;
    }

    //Implementation for custom key based cache retrieval
    public List<T> findAllInCacheSync(ContactingContext contactingContext, String cacheName, String key, Supplier<List<T>> supplier) {
        final boolean byPassCache = contactingContext.byPassCache();
        CacheOps.Flags flags = new CacheOps.Flags(byPassCache);

        return contactingCache.fromCacheableSyncFunction(
                cacheName,
                key,
                k -> supplier.get(),
                (k, v, c) -> {},
                flags);

    }

    public Optional<T> findOptionalInCacheSync(ContactingContext contactingContext, String cacheName, String key, Supplier<Optional<T>> supplier) {
        boolean byPassCache = contactingContext.byPassCache();
        CacheOps.Flags flags = new CacheOps.Flags(byPassCache);

        return contactingCache.fromCacheableSyncFunction(
                cacheName,
                key,
                k -> supplier.get(),
                (k, v, c) -> {},
                flags
        );
    }

    public void evictCache(String cacheName, String key) {
        log.info("Evicting cache {} for key {}", cacheName, key);
        contactingCache.invalidateCache(cacheName, Optional.of(key));
    }
}
