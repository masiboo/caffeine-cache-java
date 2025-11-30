package nl.ing.api.contacting.conf.repository.support;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import nl.ing.api.java.contacting.caching.core.ContactingCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheAwareRepository Tests")
class CacheAwareRepositoryTest {

    @Mock
    private ContactingCache contactingCache;

    @Mock
    private ContactingContext contactingContext;

    private TestCacheAwareRepository testRepository;

    // Test implementation of abstract class
    private static class TestCacheAwareRepository extends CacheAwareRepository<String, Long> {
        public TestCacheAwareRepository(ContactingCache contactingCache) {
            super(contactingCache);
        }
    }

    @BeforeEach
    void setUp() {
        testRepository = new TestCacheAwareRepository(contactingCache);
    }

    @Test
    @DisplayName("should find all in cache sync and call supplier")
    void shouldFindAllInCacheSyncAndCallSupplier() {
        String cacheName = "test-cache";
        String key = "test-key";
        List<String> expectedData = List.of("item1", "item2");
        Supplier<List<String>> supplier = () -> expectedData;

        // Mock the cache to return the supplier's result
        when(contactingCache.fromCacheableSyncFunction(any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> supplier.get());

        List<String> result = testRepository.findAllInCacheSync(contactingContext, cacheName, key, supplier);

        assertThat(result).containsExactly("item1", "item2");
        verify(contactingCache).fromCacheableSyncFunction(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("should handle empty supplier result")
    void shouldHandleEmptySupplierResult() {
        String cacheName = "test-cache";
        String key = "test-key";
        Supplier<List<String>> supplier = List::of;

        when(contactingCache.fromCacheableSyncFunction(any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> supplier.get());

        List<String> result = testRepository.findAllInCacheSync(contactingContext, cacheName, key, supplier);

        assertThat(result).isEmpty();
        verify(contactingCache).fromCacheableSyncFunction(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("should propagate supplier exceptions")
    void shouldPropagateSupplierExceptions() {
        String cacheName = "test-cache";
        String key = "test-key";
        RuntimeException expectedException = new RuntimeException("Database error");
        Supplier<List<String>> supplier = () -> {
            throw expectedException;
        };

        when(contactingCache.fromCacheableSyncFunction(any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> supplier.get());

        assertThatThrownBy(() -> testRepository.findAllInCacheSync(contactingContext, cacheName, key, supplier))
                .isEqualTo(expectedException);
    }

    @Test
    @DisplayName("should evict cache for specific key")
    void shouldEvictCacheForSpecificKey() {
        String cacheName = "test-cache";
        String key = "test-key";

        testRepository.evictCache(cacheName, key);

        verify(contactingCache).invalidateCache(cacheName, Optional.of(key));
    }

    @Test
    @DisplayName("should handle null supplier gracefully")
    void shouldHandleNullSupplierGracefully() {
        String cacheName = "test-cache";
        String key = "test-key";
        Supplier<List<String>> supplier = () -> null;

        when(contactingCache.fromCacheableSyncFunction(any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> supplier.get());

        List<String> result = testRepository.findAllInCacheSync(contactingContext, cacheName, key, supplier);

        assertThat(result).isNull();
    }
}

