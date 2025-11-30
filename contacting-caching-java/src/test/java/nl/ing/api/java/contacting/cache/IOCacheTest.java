package nl.ing.api.java.contacting.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import nl.ing.api.java.contacting.caching.util.CacheOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IOCacheTest {

    private int counter = 0;

    private CompletableFuture<String> myIoOperation(int i) {
        return CompletableFuture.supplyAsync(() -> {
            if (counter == 2) {
                throw new RuntimeException();
            }
            counter++;
            return String.valueOf(i + counter);
        });
    }

    private Cache<String, Integer> createCache(long maxSize, Duration expireAfter) {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireAfter)
                .build();
    }

    @Test
    @DisplayName("return value from cache")
    void testReturnValueFromCache() throws ExecutionException, InterruptedException {
        counter = 0;
        Cache<String, Integer> cache = createCache(5, Duration.ofSeconds(1));

        Function<String, CompletableFuture<Integer>> service = (String key) -> {
            counter++;
            return CompletableFuture.completedFuture(key.length());
        };

        Function<String, CompletableFuture<Integer>> memoized = (String key) -> CacheOps.fromCache(key, service, cache, CacheOps.Flags.DEFAULT);

        assertEquals(3, memoized.apply("123").get());
        assertEquals(3, memoized.apply("123").get());
        assertEquals(4, memoized.apply("1234").get());
        assertEquals(4, memoized.apply("1234").get());
        assertEquals(5, memoized.apply("12345").get());
        assertEquals(3, counter);

        Thread.sleep(1001);

        assertEquals(3, memoized.apply("123").get());
        assertEquals(4, counter);
    }

    @Test
    @DisplayName("io safety is maintained")
    void testIoSafetyIsMaintained() throws ExecutionException, InterruptedException {
        counter = 0;
        Cache<String, Integer> cache = createCache(5, Duration.ofSeconds(1));

        Function<String, CompletableFuture<Integer>> service = (String key) -> {
            counter++;
            return CompletableFuture.completedFuture(key.length());
        };

        Function<String, CompletableFuture<Integer>> memoized = (String key) -> CacheOps.fallBack(key, input -> {
                counter++;
                return CompletableFuture.completedFuture(input.length());

        }, cache);
        CompletableFuture<Integer> first = memoized.apply("123");
        CompletableFuture<Integer> second = memoized.apply("123");

        assertEquals(3, first.get());
        assertEquals(3, second.get());
        assertEquals(2, counter);
    }

    @Test
    @DisplayName("return failures")
    void testReturnFailures() throws ExecutionException, InterruptedException {
        counter = 0;
        Cache<String, Integer> cache = createCache(5, Duration.ofSeconds(10));

        Function<String, CompletableFuture<Integer>> service = (String key) -> {
            counter++;
            return CompletableFuture.completedFuture(key.length());
        };

        Function<String, CompletableFuture<Integer>> memoized = (String key) -> {

            if ("123".equals(key)) {
                CompletableFuture<Integer> failed = new CompletableFuture<>();
                failed.completeExceptionally(new RuntimeException());
                return failed;
            } else {
                return service.apply(key);
            }
        };

        assertEquals(3, memoized.apply("123").exceptionally(e -> 3).get());
        assertEquals(4, memoized.apply("1234").get());
        assertEquals(5, memoized.apply("12345").get());
        assertEquals(2, counter);
    }

   @Test
    @DisplayName("use fallback cache")
    void testUseFallbackCache() throws ExecutionException, InterruptedException {
        counter = 0;

        Cache<String, Integer> cache = createCache(5, Duration.ofSeconds(2));

        Function<String, CompletableFuture<Integer>> memoized = key -> CacheOps.fallBack(key, input -> {
            if (counter >= 5) {
                CompletableFuture<Integer> failed = new CompletableFuture<>();
                failed.completeExceptionally(new RuntimeException());
                return failed;
            } else {
                counter++;
                return CompletableFuture.completedFuture(input.length());
            }
        }, cache);

        assertEquals(3, memoized.apply("123").get());
        assertEquals(3, memoized.apply("123").get());
        assertEquals(4, memoized.apply("1234").get());
        assertEquals(4, memoized.apply("1234").get());
        assertEquals(5, memoized.apply("12345").get());

        assertEquals(5, counter);

        assertEquals(3, memoized.apply("123").get());
        assertEquals(3, memoized.apply("123").get());
        assertEquals(4, memoized.apply("1234").get());
        assertEquals(4, memoized.apply("1234").get());
        assertEquals(5, memoized.apply("12345").get());

        assertEquals(5, counter);

        assertEquals("error", memoized.apply("123456").handle((v, ex) -> ex != null ? "error" : v).get());

        Thread.sleep(2001);
        assertEquals("error-again", memoized.apply("123456").handle((v, ex) -> ex != null ? "error-again" : v).get());
        assertEquals(5, counter);
    }

    @Test
    @DisplayName("skip the cache")
    void testSkipTheCache() throws ExecutionException, InterruptedException {
        counter = 0;
        CacheOps.Flags flags = new CacheOps.Flags(true);
        Cache<String, Integer> cache = createCache(5, Duration.ofSeconds(1));

        Function<String, CompletableFuture<Integer>> service = (String key) -> {
            counter++;
            return CompletableFuture.completedFuture(key.length());
        };

        Function<String, CompletableFuture<Integer>> memoized = (String key) -> CacheOps.fromCache(key, service, cache, flags);

        assertEquals(3, memoized.apply("123").get());
        assertEquals(3, memoized.apply("123").get());
        assertEquals(4, memoized.apply("1234").get());
        assertEquals(4, memoized.apply("1234").get());
        assertEquals(5, memoized.apply("12345").get());
        assertEquals(5, counter);

        Thread.sleep(1001);

        assertEquals(3, memoized.apply("123").get());
        assertEquals(6, counter);
    }
}
