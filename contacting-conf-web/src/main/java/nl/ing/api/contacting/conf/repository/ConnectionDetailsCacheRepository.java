package nl.ing.api.contacting.conf.repository;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.ConnectionDetailsEntity;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsDTO;
import nl.ing.api.contacting.conf.repository.support.CacheAwareRepository;
import nl.ing.api.contacting.conf.util.CacheType;
import nl.ing.api.java.contacting.caching.core.ContactingCache;
import org.springframework.stereotype.Repository;
import zio.logging.log;

import java.util.List;
import java.util.function.Supplier;

@Repository
@Slf4j
public class ConnectionDetailsCacheRepository extends CacheAwareRepository<ConnectionDetailsDTO, Long> {

    /*Declaring a custom prefix here since contactintgContext.account()
    is not used for eviction so no need to pass contactingContext around;
     */
    private final static String cacheKeyPrefix = "-for-account-";
    private final ConnectionDetailsJpaRepository jpaRepository;

    public ConnectionDetailsCacheRepository(ContactingCache contactingCache, ConnectionDetailsJpaRepository jpaRepository) {
        super(contactingCache);
        this.jpaRepository = jpaRepository;
    }

    public List<ConnectionDetailsDTO> findAllForAccount(ContactingContext contactingContext) {

        final Long accountId = contactingContext.accountId();

        return findAllInCacheSync(
                contactingContext,
                CacheType.ACTIVE_CONNECTION.cacheName(),
                cacheKeyPrefix.concat(String.valueOf(accountId)),
                () -> jpaRepository.findAllDataByAccountId(accountId)
        );
    }

    public ConnectionDetailsEntity save(Long accountId, ConnectionDetailsEntity connectionDetailsEntity) {
        ConnectionDetailsEntity savedEntity = jpaRepository.save(connectionDetailsEntity);

        try {
            evictCache(CacheType.ACTIVE_CONNECTION.cacheName(), cacheKeyPrefix.concat(String.valueOf(accountId)));
        } catch (Exception e) {
            log.error("Failed to evict cache for account {}: {}", accountId, e.getMessage());
        }

        return savedEntity;
    }

    public void evictCacheAfterUpdate(Long accountId) {
        log.info("Evicting cache for key {}", cacheKeyPrefix.concat(String.valueOf(accountId)));
        evictCache(CacheType.ACTIVE_CONNECTION.cacheName(), cacheKeyPrefix.concat(String.valueOf(accountId)));
    }

}
