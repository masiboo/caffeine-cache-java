package nl.ing.api.contacting.conf.repository;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import nl.ing.api.contacting.conf.domain.entity.PlatformAccountSettingsEntity;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformAccountSettingsCacheRepository Tests")
class PlatformAccountSettingsCacheRepositoryTest {

    @Mock
    private ContactingCache contactingCache;

    @Mock
    private PlatformAccountSettingsJpaRepository jpaRepository;

    @Mock
    private ContactingContext contactingContext;

    private PlatformAccountSettingsCacheRepository platformAccountSettingsCacheRepository;

    private PlatformAccountSettingsEntity themeSetting;
    private PlatformAccountSettingsEntity languageSetting;
    private List<PlatformAccountSettingsEntity> testSettings;

    @BeforeEach
    void setUp() {
        platformAccountSettingsCacheRepository = new PlatformAccountSettingsCacheRepository(
                contactingCache, jpaRepository);

        themeSetting = PlatformAccountSettingsEntity.builder()
                .id(1L)
                .key("platform.theme")
                .value("dark")
                .accountId(123L)
                .build();

        languageSetting = PlatformAccountSettingsEntity.builder()
                .id(2L)
                .key("platform.language")
                .value("en")
                .accountId(123L)
                .build();

        testSettings = List.of(themeSetting, languageSetting);
    }

    @Test
    @DisplayName("should find settings by account ID with cache hit")
    void shouldFindByAccountIdWithCacheHit() {
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenReturn(testSettings);

        List<PlatformAccountSettingsEntity> result = platformAccountSettingsCacheRepository
                .findByAccountId(contactingContext);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(themeSetting, languageSetting);
        verify(contactingCache).fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("should find settings by account ID with cache miss")
    void shouldFindByAccountIdWithCacheMiss() {
        when(jpaRepository.findByAccountId(123L)).thenReturn(testSettings);
        when(contactingContext.accountId()).thenReturn(123L);
        when(contactingContext.accountCacheKey()).thenReturn("account:123");
        when(contactingContext.byPassCache()).thenReturn(false);
        // Simulate cache miss by calling the supplier function
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenAnswer(invocation -> {
            Function<String, List<PlatformAccountSettingsEntity>> supplier = invocation.getArgument(2);
            return supplier.apply("account:123");
        });

        List<PlatformAccountSettingsEntity> result = platformAccountSettingsCacheRepository
                .findByAccountId(contactingContext);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(themeSetting, languageSetting);
        verify(jpaRepository).findByAccountId(123L);
    }

    @Test
    @DisplayName("should find settings by account ID with cache bypass")
    void shouldFindByAccountIdWithCacheBypass() {
        when(contactingContext.byPassCache()).thenReturn(true);
        when(jpaRepository.findByAccountId(123L)).thenReturn(testSettings);
        when(contactingContext.accountId()).thenReturn(123L);
        when(contactingContext.accountCacheKey()).thenReturn("account:123");
        when(contactingContext.byPassCache()).thenReturn(false);
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenAnswer(invocation -> {
            Function<String, List<PlatformAccountSettingsEntity>> supplier = invocation.getArgument(2);
            return supplier.apply("account:123");
        });

        List<PlatformAccountSettingsEntity> result = platformAccountSettingsCacheRepository
                .findByAccountId(contactingContext);

        assertThat(result).hasSize(2);
        verify(contactingCache).fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("should find entity by ID when exists in cached list")
    void shouldFindByIdWhenExistsInCachedList() {
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenReturn(testSettings);

        Optional<PlatformAccountSettingsEntity> result = platformAccountSettingsCacheRepository
                .findById(1L, contactingContext);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(themeSetting);
        assertThat(result.get().getKey()).isEqualTo("platform.theme");
    }

    @Test
    @DisplayName("should return empty when finding by non-existing ID")
    void shouldReturnEmptyWhenFindingByNonExistingId() {
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenReturn(testSettings);

        Optional<PlatformAccountSettingsEntity> result = platformAccountSettingsCacheRepository
                .findById(999L, contactingContext);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should save entity and evict cache")
    void shouldSaveEntityAndEvictCache() {
        PlatformAccountSettingsEntity newSetting = PlatformAccountSettingsEntity.builder()
                .key("platform.timezone")
                .value("UTC")
                .accountId(123L)
                .build();

        PlatformAccountSettingsEntity savedSetting = PlatformAccountSettingsEntity.builder()
                .id(3L)
                .key("platform.timezone")
                .value("UTC")
                .accountId(123L)
                .build();

        when(jpaRepository.save(newSetting)).thenReturn(savedSetting);

        when(contactingContext.accountCacheKey()).thenReturn("account:123");
        PlatformAccountSettingsEntity result = platformAccountSettingsCacheRepository
                .save(newSetting, contactingContext);

        assertThat(result).isEqualTo(savedSetting);
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getKey()).isEqualTo("platform.timezone");

        verify(jpaRepository).save(newSetting);
        verify(contactingCache).invalidateCache(
                CacheType.PLATFORM_ACCOUNT_SETTINGS.cacheName(),
                Optional.of("account:123")
        );
    }

    @Test
    @DisplayName("should handle empty cached list gracefully")
    void shouldHandleEmptyCachedListGracefully() {
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenReturn(List.of());

        List<PlatformAccountSettingsEntity> result = platformAccountSettingsCacheRepository
                .findByAccountId(contactingContext);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should use correct cache parameters for findByAccountId")
    void shouldUseCorrectCacheParametersForFindByAccountId() {
        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenReturn(testSettings);

        platformAccountSettingsCacheRepository.findByAccountId(contactingContext);

        verify(contactingCache).fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("should filter correctly when finding by ID from multiple cached entities")
    void shouldFilterCorrectlyWhenFindingByIdFromMultipleCachedEntities() {
        // Add more entities to test filtering
        PlatformAccountSettingsEntity currencySetting = PlatformAccountSettingsEntity.builder()
                .id(3L)
                .key("platform.currency")
                .value("USD")
                .accountId(123L)
                .build();

        List<PlatformAccountSettingsEntity> multipleSettings = List.of(
                themeSetting, languageSetting, currencySetting
        );

        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenReturn(multipleSettings);

        Optional<PlatformAccountSettingsEntity> result = platformAccountSettingsCacheRepository
                .findById(2L, contactingContext);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(languageSetting);
        assertThat(result.get().getKey()).isEqualTo("platform.language");
        assertThat(result.get().getValue()).isEqualTo("en");
    }

    @Test
    @DisplayName("should handle save operation with null entity gracefully")
    void shouldHandleSaveOperationWithNullEntityGracefully() {
        when(jpaRepository.save(any())).thenThrow(new IllegalArgumentException("Entity cannot be null"));
        when(contactingContext.accountCacheKey()).thenReturn("account:123"); // Add this line

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                platformAccountSettingsCacheRepository.save(null, contactingContext)
        ).getMessage()).isEqualTo("Entity cannot be null");

        verify(jpaRepository).save(any());
        // Cache should still be evicted despite the exception
        verify(contactingCache).invalidateCache(
                CacheType.PLATFORM_ACCOUNT_SETTINGS.cacheName(),
                Optional.of("account:123")
        );
    }


    @Test
    @DisplayName("should evict cache even when save operation fails")
    void shouldEvictCacheEvenWhenSaveOperationFails() {
        PlatformAccountSettingsEntity newSetting = PlatformAccountSettingsEntity.builder()
                .key("platform.invalid")
                .value("invalid")
                .accountId(123L)
                .build();

        when(jpaRepository.save(newSetting)).thenThrow(new RuntimeException("Database error"));
        when(contactingContext.accountCacheKey()).thenReturn("account:123"); // Add this line

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                platformAccountSettingsCacheRepository.save(newSetting, contactingContext)
        ).getMessage()).isEqualTo("Database error");

        verify(jpaRepository).save(newSetting);
        verify(contactingCache).invalidateCache(
                CacheType.PLATFORM_ACCOUNT_SETTINGS.cacheName(),
                Optional.of("account:123")
        );
    }


    @Test
    @DisplayName("should handle different account contexts correctly")
    void shouldHandleDifferentAccountContextsCorrectly() {
        ContactingContext anotherContext = mock(ContactingContext.class);
        when(anotherContext.accountId()).thenReturn(456L);
        when(anotherContext.accountCacheKey()).thenReturn("account:456");
        when(anotherContext.byPassCache()).thenReturn(false);

        List<PlatformAccountSettingsEntity> anotherAccountSettings = List.of(
                PlatformAccountSettingsEntity.builder()
                        .id(4L)
                        .key("platform.theme")
                        .value("light")
                        .accountId(456L)
                        .build()
        );

        when(contactingCache.fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        )).thenReturn(anotherAccountSettings);

        List<PlatformAccountSettingsEntity> result = platformAccountSettingsCacheRepository
                .findByAccountId(anotherContext);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountId()).isEqualTo(456L);
        assertThat(result.get(0).getValue()).isEqualTo("light");

        verify(contactingCache).fromCacheableSyncFunction(
                any(), any(), any(), any(), any()
        );
    }
}

