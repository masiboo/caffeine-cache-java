package nl.ing.api.contacting.conf.repository;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.cassandra.BusinessFunctionOnTeamEntity;
import nl.ing.api.contacting.conf.domain.model.permission.BusinessFunctionVO;
import nl.ing.api.contacting.conf.repository.support.CacheAwareRepository;
import nl.ing.api.contacting.conf.util.CacheType;
import nl.ing.api.java.contacting.caching.core.ContactingCache;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Repository
@Slf4j
public class PermissionCacheRepository extends CacheAwareRepository {

    private final BusinessFunctionOnTeamRepository jpaCassandraRepository;

    public PermissionCacheRepository(ContactingCache contactingCache, BusinessFunctionOnTeamRepository jpaCassandraRepository) {
        super(contactingCache);
        this.jpaCassandraRepository = jpaCassandraRepository;
    }

    public List<BusinessFunctionOnTeamEntity> findByAccountFriendlyNameCache(ContactingContext contactingContext, String accountFriendlyName) {

        Supplier<List<BusinessFunctionOnTeamEntity>> supplier = () ->
        {
            log.info("Cache miss/bypass for accountId in BusinessFunctionOnTeamEntity {}", accountFriendlyName);
            return jpaCassandraRepository.findByAccountFriendlyName(accountFriendlyName);
        };


        return findAllInCacheSync(
                contactingContext,
                CacheType.CONNECTING_PERMISSION.cacheName(),
                contactingContext.accountId().toString().concat(String.valueOf(accountFriendlyName)),
                supplier
        );
    }

    public List<BusinessFunctionOnTeamEntity> upsertAll(ContactingContext contactingContext, String accountFriendlyName, List<BusinessFunctionOnTeamEntity> entities) {
        List<BusinessFunctionOnTeamEntity> savedEntities = Optional.ofNullable(findByAccountFriendlyNameCache(contactingContext, accountFriendlyName))
                                                            .orElse(Collections.emptyList());
        if (savedEntities.isEmpty()) {
            evictCache(CacheType.CONNECTING_PERMISSION.cacheName(), CacheType.CONNECTING_PERMISSION.cacheName().concat(accountFriendlyName));
            log.warn("Error in deleting data into db, reverting DCache delete");
            return List.of();
        }
        return savedEntities;
    }

    public void deletePermission(BusinessFunctionVO businessFunctionVO, String accountFriendlyName) {
        evictCacheAfterUpdate(accountFriendlyName);
        jpaCassandraRepository.deleteByAccountFriendlyNameAndBusinessFunctionAndOrganisationIdAndRole(
                accountFriendlyName,
                businessFunctionVO.businessFunction(),
                businessFunctionVO.organisationId(),
                businessFunctionVO.role()
        );
    }

    public void evictCacheAfterUpdate(String accountFriendlyName) {
        evictCache(CacheType.CONNECTING_PERMISSION.cacheName(), CacheType.CONNECTING_PERMISSION.cacheName().concat(accountFriendlyName));
    }

}
