package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.AccountSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountSettingsJpaRepository extends JpaRepository<AccountSettingsEntity, Long> {

    // Using derived query method with 'Containing' keyword
    List<AccountSettingsEntity> findByAccountIdAndCapabilitiesContaining(Long accountId, String capability);
    // Using derived query method with 'Containing' keyword
    List<AccountSettingsEntity> findByAccountIdAndConsumersContaining(Long accountId, String consumer);
    List<AccountSettingsEntity> findByAccountId(Long accountId);
    Optional<AccountSettingsEntity> findByIdAndAccountId(Long id, Long accountId);
    List<AccountSettingsEntity> findByAccountIdAndKey(Long accountId, String key);

    @Modifying
    @Query("""
                UPDATE AccountSettingsEntity a
                SET a.value = :value
                WHERE a.key = :key AND a.accountId = :accountId
            """)
    int updateSettingValue(@Param("key") String key, @Param("value") String value, @Param("accountId") Long accountId);
}
