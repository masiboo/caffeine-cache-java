package nl.ing.api.contacting.conf.repository;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.PlatformAccountSettingsEntity;
import nl.ing.api.contacting.conf.repository.support.CacheAwareRepository;
import nl.ing.api.contacting.conf.util.CacheType;
import nl.ing.api.java.contacting.caching.core.ContactingCache;
import org.springframework.stereotype.Repository;
import zio.logging.log;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Repository
public class PlatformAccountSettingsCacheRepository extends CacheAwareRepository {

    private final PlatformAccountSettingsJpaRepository jpaRepository;

    public PlatformAccountSettingsCacheRepository(ContactingCache contactingCache, PlatformAccountSettingsJpaRepository jpaRepository) {
        super(contactingCache);
        this.jpaRepository = jpaRepository;
    }

    public List<PlatformAccountSettingsEntity> findByAccountId(ContactingContext contactingContext) {

        final Long accountId = contactingContext.accountId();

        return findAllInCacheSync(
                contactingContext,
                CacheType.PLATFORM_ACCOUNT_SETTINGS.cacheName(),
                contactingContext.accountCacheKey(),
                () -> jpaRepository.findByAccountId(accountId)
        );
    }

    public Optional<PlatformAccountSettingsEntity> findById(Long id, ContactingContext contactingContext) {
        List<PlatformAccountSettingsEntity> settings = findByAccountId(contactingContext);
        return settings.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    public PlatformAccountSettingsEntity save(PlatformAccountSettingsEntity platformAccountSettingsEntity, ContactingContext contactingContext) {
        try {
            PlatformAccountSettingsEntity savedEntity = jpaRepository.save(platformAccountSettingsEntity);
            return savedEntity;
        } finally {
            evictCache(CacheType.PLATFORM_ACCOUNT_SETTINGS.cacheName(), contactingContext.accountCacheKey());
        }
    }

}
