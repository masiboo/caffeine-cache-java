package nl.ing.api.contacting.conf.repository;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.AttributeEntity;
import nl.ing.api.contacting.conf.repository.support.CacheAwareRepository;
import nl.ing.api.contacting.conf.util.CacheType;
import nl.ing.api.java.contacting.caching.core.ContactingCache;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Repository
@Slf4j
public class AttributeCacheRepository extends CacheAwareRepository {

    private static final String CACHE_KEY_PREFIX = "attributes-by-account-";
    private final AttributeJpaRepository jpaRepository;

    public AttributeCacheRepository(ContactingCache contactingCache, AttributeJpaRepository jpaRepository) {
        super(contactingCache);
        this.jpaRepository = jpaRepository;
    }

    public List<AttributeEntity> findAllForAccount(ContactingContext contactingContext) {
        Long accountId = contactingContext.accountId();

        return findAllInCacheSync(
                contactingContext,
                CacheType.ATTRIBUTES_BY_ACCOUNT.cacheName(),
                CACHE_KEY_PREFIX.concat(String.valueOf(accountId)),
                () -> jpaRepository.findByAccountId(accountId)
        );
    }

    public Optional<AttributeEntity> findById(Long attributeId, ContactingContext contactingContext) {

        return findOptionalInCacheSync(
                contactingContext,
                CacheType.ATTRIBUTES_BY_ACCOUNT.cacheName(),
                CACHE_KEY_PREFIX.concat(String.valueOf(attributeId)),
                () -> jpaRepository.findById(attributeId)
        );
    }

}