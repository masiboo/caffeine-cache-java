package nl.ing.api.contacting.conf.repository;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import nl.ing.api.contacting.conf.domain.entity.ConnectionDetailsEntity;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsDTO;
import nl.ing.api.contacting.conf.util.CacheType;
import nl.ing.api.java.contacting.caching.core.ContactingCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConnectionDetailsCacheRepository Tests")
class ConnectionDetailsCacheRepositoryTest {

    @Mock
    private ContactingCache contactingCache;

    @Mock
    private ConnectionDetailsJpaRepository jpaRepository;

    @Mock
    private ContactingContext contactingContext;

    private ConnectionDetailsCacheRepository connectionDetailsCacheRepository;

    private ConnectionDetailsDTO testConnectionDetailsDTO;
    private ConnectionDetailsEntity testConnectionDetailsEntity;
    private List<ConnectionDetailsDTO> testConnectionDetailsList;

    @BeforeEach
    void setUp() {
        connectionDetailsCacheRepository = new ConnectionDetailsCacheRepository(
                contactingCache, jpaRepository);

        // Create test data
        testConnectionDetailsDTO = mock(ConnectionDetailsDTO.class);
        testConnectionDetailsList = List.of(testConnectionDetailsDTO);

        testConnectionDetailsEntity = ConnectionDetailsEntity.builder()
                .id(1L)
                .connectionType("REST")
                .edgeLocation("US-EAST")
                .url("https://api.example.com")
                .region("us-east-1")
                .build();
    }

    @Test
    @DisplayName("should find all connections for account with cache hit")
    void shouldFindAllConnectionsForAccountWithCacheHit() {
        when(contactingContext.accountId()).thenReturn(123L);
        when(contactingContext.byPassCache()).thenReturn(false);
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenReturn(testConnectionDetailsList);

        List<ConnectionDetailsDTO> result = connectionDetailsCacheRepository
                .findAllForAccount(contactingContext);

        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(testConnectionDetailsDTO);
        verify(contactingCache).fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("should find all connections for account with cache miss")
    void shouldFindAllConnectionsForAccountWithCacheMiss() {
        when(contactingContext.accountId()).thenReturn(123L);
        when(contactingContext.byPassCache()).thenReturn(false);
        when(jpaRepository.findAllDataByAccountId(123L)).thenReturn(testConnectionDetailsList);

        // Simulate cache miss by calling the supplier function
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenAnswer(invocation -> {
            Function<String, List<ConnectionDetailsDTO>> supplier = invocation.getArgument(2);
            return supplier.apply("-for-account-123");
        });

        List<ConnectionDetailsDTO> result = connectionDetailsCacheRepository
                .findAllForAccount(contactingContext);

        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(testConnectionDetailsDTO);
        verify(jpaRepository).findAllDataByAccountId(123L);
    }

    @Test
    @DisplayName("should find all connections for account with cache bypass")
    void shouldFindAllConnectionsForAccountWithCacheBypass() {
        when(contactingContext.accountId()).thenReturn(123L);
        when(contactingContext.byPassCache()).thenReturn(true);
        when(jpaRepository.findAllDataByAccountId(123L)).thenReturn(testConnectionDetailsList);

        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenAnswer(invocation -> {
            Function<String, List<ConnectionDetailsDTO>> supplier = invocation.getArgument(2);
            return supplier.apply("-for-account-123");
        });

        List<ConnectionDetailsDTO> result = connectionDetailsCacheRepository
                .findAllForAccount(contactingContext);

        assertThat(result).hasSize(1);
        verify(contactingCache).fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("should handle empty cached list gracefully")
    void shouldHandleEmptyCachedListGracefully() {
        when(contactingContext.accountId()).thenReturn(123L);
        when(contactingContext.byPassCache()).thenReturn(false);
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenReturn(List.of());

        List<ConnectionDetailsDTO> result = connectionDetailsCacheRepository
                .findAllForAccount(contactingContext);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should save entity and evict cache")
    void shouldSaveEntityAndEvictCache() {
        ConnectionDetailsEntity savedEntity = ConnectionDetailsEntity.builder()
                .id(2L)
                .connectionType("REST")
                .edgeLocation("US-EAST")
                .url("https://api.example.com")
                .region("us-east-1")
                .build();

        when(jpaRepository.save(testConnectionDetailsEntity)).thenReturn(savedEntity);

        ConnectionDetailsEntity result = connectionDetailsCacheRepository
                .save(123L, testConnectionDetailsEntity);

        assertThat(result).isEqualTo(savedEntity);
        assertThat(result.getId()).isEqualTo(2L);

        verify(jpaRepository).save(testConnectionDetailsEntity);
        verify(contactingCache).invalidateCache(
                CacheType.ACTIVE_CONNECTION.cacheName(),
                Optional.of("-for-account-123")
        );
    }

    @Test
    @DisplayName("should use correct cache parameters for findAllForAccount")
    void shouldUseCorrectCacheParametersForFindAllForAccount() {
        when(contactingContext.accountId()).thenReturn(456L);
        when(contactingContext.byPassCache()).thenReturn(false);
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenReturn(testConnectionDetailsList);

        connectionDetailsCacheRepository.findAllForAccount(contactingContext);

        verify(contactingCache).fromCacheableSyncFunction(
                eq(CacheType.ACTIVE_CONNECTION.cacheName()),
                eq("-for-account-456"),
                any(),
                any(),
                any()
        );
    }

    @Test
    @DisplayName("should handle different account contexts correctly")
    void shouldHandleDifferentAccountContextsCorrectly() {
        ContactingContext anotherContext = mock(ContactingContext.class);
        when(anotherContext.accountId()).thenReturn(789L);
        when(anotherContext.byPassCache()).thenReturn(false);

        ConnectionDetailsDTO anotherConnectionDto = mock(ConnectionDetailsDTO.class);
        List<ConnectionDetailsDTO> anotherAccountConnections = List.of(anotherConnectionDto);

        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenReturn(anotherAccountConnections);

        List<ConnectionDetailsDTO> result = connectionDetailsCacheRepository
                .findAllForAccount(anotherContext);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(anotherConnectionDto);

        verify(contactingCache).fromCacheableSyncFunction(
                eq(CacheType.ACTIVE_CONNECTION.cacheName()),
                eq("-for-account-789"),
                any(),
                any(),
                any()
        );
    }

    @Test
    @DisplayName("should evict cache after update")
    void shouldEvictCacheAfterUpdate() {
        connectionDetailsCacheRepository.evictCacheAfterUpdate(555L);

        verify(contactingCache).invalidateCache(
                CacheType.ACTIVE_CONNECTION.cacheName(),
                Optional.of("-for-account-555")
        );
    }

    @Test
    @DisplayName("should handle save operation with cache eviction failure gracefully")
    void shouldHandleSaveOperationWithCacheEvictionFailureGracefully() {
        ConnectionDetailsEntity savedEntity = ConnectionDetailsEntity.builder()
                .id(3L)
                .connectionType("WebSocket")
                .edgeLocation("EU-WEST")
                .url("wss://websocket.example.com")
                .region("eu-west-1")
                .build();

        when(jpaRepository.save(testConnectionDetailsEntity)).thenReturn(savedEntity);
        doThrow(new RuntimeException("Cache eviction failed"))
                .when(contactingCache).invalidateCache(any(), any());

        // Should still return saved entity even if cache eviction fails
        ConnectionDetailsEntity result = connectionDetailsCacheRepository
                .save(123L, testConnectionDetailsEntity);

        assertThat(result).isEqualTo(savedEntity);
        verify(jpaRepository).save(testConnectionDetailsEntity);
        verify(contactingCache).invalidateCache(
                CacheType.ACTIVE_CONNECTION.cacheName(),
                Optional.of("-for-account-123")
        );
    }

    @Test
    @DisplayName("should handle large account IDs correctly in cache key generation")
    void shouldHandleLargeAccountIdsCorrectlyInCacheKeyGeneration() {
        Long largeAccountId = 9999999999L;
        when(contactingContext.accountId()).thenReturn(largeAccountId);
        when(contactingContext.byPassCache()).thenReturn(false);
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenReturn(testConnectionDetailsList);

        connectionDetailsCacheRepository.findAllForAccount(contactingContext);

        verify(contactingCache).fromCacheableSyncFunction(
                eq(CacheType.ACTIVE_CONNECTION.cacheName()),
                eq("-for-account-9999999999"),
                any(),
                any(),
                any()
        );
    }

    @Test
    @DisplayName("should handle multiple save operations for same account")
    void shouldHandleMultipleSaveOperationsForSameAccount() {
        ConnectionDetailsEntity entity1 = ConnectionDetailsEntity.builder()
                .connectionType("REST")
                .edgeLocation("US-EAST")
                .url("https://api1.example.com")
                .region("us-east-1")
                .build();

        ConnectionDetailsEntity entity2 = ConnectionDetailsEntity.builder()
                .connectionType("GraphQL")
                .edgeLocation("US-WEST")
                .url("https://api2.example.com")
                .region("us-west-1")
                .build();

        ConnectionDetailsEntity saved1 = ConnectionDetailsEntity.builder()
                .id(4L)
                .connectionType("REST")
                .edgeLocation("US-EAST")
                .url("https://api1.example.com")
                .region("us-east-1")
                .build();

        ConnectionDetailsEntity saved2 = ConnectionDetailsEntity.builder()
                .id(5L)
                .connectionType("GraphQL")
                .edgeLocation("US-WEST")
                .url("https://api2.example.com")
                .region("us-west-1")
                .build();

        when(jpaRepository.save(entity1)).thenReturn(saved1);
        when(jpaRepository.save(entity2)).thenReturn(saved2);

        Long accountId = 888L;

        // Save both entities
        ConnectionDetailsEntity result1 = connectionDetailsCacheRepository.save(accountId, entity1);
        ConnectionDetailsEntity result2 = connectionDetailsCacheRepository.save(accountId, entity2);

        assertThat(result1).isEqualTo(saved1);
        assertThat(result2).isEqualTo(saved2);

        // Verify cache was evicted for both operations
        verify(contactingCache, times(2)).invalidateCache(
                CacheType.ACTIVE_CONNECTION.cacheName(),
                Optional.of("-for-account-888")
        );
    }

    @Test
    @DisplayName("should handle null account ID gracefully")
    void shouldHandleNullAccountIdGracefully() {
        when(contactingContext.accountId()).thenReturn(null);
        when(contactingContext.byPassCache()).thenReturn(false);

        // Should handle null account ID without throwing exception
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenAnswer(invocation -> {
            Function<String, List<ConnectionDetailsDTO>> supplier = invocation.getArgument(2);
            return supplier.apply("-for-account-null");
        });

        when(jpaRepository.findAllDataByAccountId(null)).thenReturn(List.of());

        List<ConnectionDetailsDTO> result = connectionDetailsCacheRepository
                .findAllForAccount(contactingContext);

        assertThat(result).isEmpty();
        verify(jpaRepository).findAllDataByAccountId(null);
    }
}
