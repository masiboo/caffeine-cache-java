package nl.ing.api.java.contacting.business;

import java.util.concurrent.CompletableFuture;

/**
 * Provides cache configuration for read/write operations.
 */
public interface CacheConfigProvider {

    /**
     * Should read from cache? Yes if oracle is down.
     *
     * @return CompletableFuture\<Boolean\> indicating if cache read is enabled.
     */
    CompletableFuture<Boolean> isReadEnabled();

    /**
     * Should write to cache?
     *
     * @return CompletableFuture\<Boolean\> indicating if cache write is enabled.
     */
    CompletableFuture<Boolean> isWriteEnabled();
}
