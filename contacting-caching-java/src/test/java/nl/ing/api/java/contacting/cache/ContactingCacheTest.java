package nl.ing.api.java.contacting.cache;

import nl.ing.api.java.contacting.caching.core.ContactingCache;
import nl.ing.api.java.contacting.caching.util.CacheOps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ContactingCacheTest {

    static class TestCache extends ContactingCache {

        @Override
        public com.typesafe.config.Config config() {
            return com.typesafe.config.ConfigFactory.empty();
        }

    }

    private ContactingCache cache;

    @BeforeEach
    void setup() {
        cache = new TestCache();
        cache.clearCache("testCache");

    }

    @AfterEach
    void tearDown() {
        cache.clearCache("testCache");
    }

    @Test
    void fromCacheableSyncFunction_shouldReturnValue() {

        Function<String, String> loader = k -> "value";
        String result = cache.fromCacheableSyncFunction("testCache", "key", loader, (k, v, c) -> {
        }, CacheOps.Flags.DEFAULT);
        assertEquals("value", result);

    }

    @Test
    void fallBack_shouldReturnValueAndCacheIt() {

        final int[] count = {0};
        Function<String, String> loader = k -> {
            count[0]++;
            if (count[0] == 1) return "first";
            throw new RuntimeException("fail");
        };

        String val1 = cache.fallBack("testCache", "key", loader, (k, v, c) -> {
        });

        assertEquals("first", val1);
        String val2 = cache.fallBack("testCache", "key", loader, (k, v, c) -> {
        });
        assertEquals("first", val2); // fallback to cached value

    }

    @Test
    void fromCacheableFunction_shouldReturnAsyncValue() throws ExecutionException, InterruptedException {

        Function<String, CompletableFuture<String>> loader = k -> CompletableFuture.completedFuture("asyncValue");
        CompletableFuture<String> future = cache.fromCacheableFunction("testCache", "key", loader, (k, v, c) -> {
        }, CacheOps.Flags.DEFAULT);
        assertEquals("asyncValue", future.get());

    }

    @Test
    void fallBackAsyncF_shouldReturnAsyncValueAndFallback() throws ExecutionException, InterruptedException {

        final int[] count = {0};

        Function<String, CompletableFuture<String>> loader = k -> {
            count[0]++;
            if (count[0] == 1) return CompletableFuture.completedFuture("first");
            CompletableFuture<String> failed = new CompletableFuture<>();
            failed.completeExceptionally(new RuntimeException("fail"));
            return failed;

        };

        CompletableFuture<String> val1 = cache.fallBackAsyncF("testCache", "key", loader, (k, v, c) -> {
        });
        assertEquals("first", val1.get());
        CompletableFuture<String> val2 = cache.fallBackAsyncF("testCache", "key", loader, (k, v, c) -> {
        });
        assertEquals("first", val2.get()); // fallback cached

    }

    @Test
    void clearCache_shouldRemoveCache() {

        cache.fromCacheableSyncFunction("testCache", "key", k -> "val", (k, v, c) -> {
        }, CacheOps.Flags.DEFAULT);

        Optional<Object> removed = cache.clearCache("testCache");
        assertTrue(removed.isPresent());

    }

    @Test
    void invalidateCache_byKey_shouldInvalidateEntry() {

        cache.fromCacheableSyncFunction("testCache", "key", k -> "val", (k, v, c) -> {
        }, CacheOps.Flags.DEFAULT);

        Optional<Object> result = cache.invalidateCache("testCache", Optional.of("key"));
        assertTrue(result.isPresent());

    }

    @Test
    void invalidateCache_all_shouldInvalidateAll() {

        cache.fromCacheableSyncFunction("testCache", "key", k -> "val", (k, v, c) -> {
        }, CacheOps.Flags.DEFAULT);

        Optional<Object> result = cache.invalidateCache("testCache", Optional.empty());
        assertTrue(result.isPresent());
        assertEquals("testCache", result.get());

    }

    @Test
    void fromCacheableSyncFunction_skipCache_shouldAlwaysReload() {

        CacheOps.Flags skipFlag = new CacheOps.Flags(true);

        final int[] count = {0};
        Function<String, String> loader = k -> {
            count[0]++;
            return "val" + count[0];

        };

        String v1 = cache.fromCacheableSyncFunction("testCache", "key", loader, (k, v, c) -> {
        }, skipFlag);
        String v2 = cache.fromCacheableSyncFunction("testCache", "key", loader, (k, v, c) -> {
        }, skipFlag);
        assertEquals("val1", v1);
        assertEquals("val2", v2);

    }

    @Test
    void fromCacheableFunction_skipCache_shouldAlwaysReload() throws ExecutionException, InterruptedException {

        CacheOps.Flags skipFlag = new CacheOps.Flags(true);
        final int[] count = {0};
        Function<String, CompletableFuture<String>> loader = k -> {
            count[0]++;
            return CompletableFuture.completedFuture("val" + count[0]);

        };
        CompletableFuture<String> v1 = cache.fromCacheableFunction("testCache", "key", loader, (k, v, c) -> {
        }, skipFlag);
        CompletableFuture<String> v2 = cache.fromCacheableFunction("testCache", "key", loader, (k, v, c) -> {
        }, skipFlag);
        assertEquals("val1", v1.get());
        assertEquals("val2", v2.get());

    }

}

