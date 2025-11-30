package nl.ing.api.contacting.conf.repository;

import com.ing.api.contacting.dto.java.audit.AuditedEntity;
import com.ing.api.contacting.dto.java.context.ContactingContext;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.AccountSettingsEntity;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.conf.domain.model.webhook.UpdateSetting;
import nl.ing.api.contacting.conf.repository.support.AuditAwareRepository;
import nl.ing.api.contacting.java.repository.jpa.audit.AuditEntityActions;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for managing AccountSettingsEntity with auditing capabilities.
 */
@Repository
@Slf4j
public class AccountSettingsAuditRepository extends AuditAwareRepository<AccountSettingsEntity, Long> {

    private final AccountSettingsJpaRepository jpaRepository;

    public AccountSettingsAuditRepository(AccountSettingsJpaRepository jpaRepository,
                                          AuditEntityActions<AccountSettingsEntity, Long> auditActions) {
        super(jpaRepository, auditActions);
        this.jpaRepository = jpaRepository;
    }

    public List<AccountSettingsEntity> findByCapabilities(Set<SettingCapability> capabilities, Long accountId) {
        return capabilities.stream()
                .flatMap(capability -> jpaRepository.findByAccountIdAndCapabilitiesContaining(accountId, capability.value()).stream())
                .distinct()
                .toList();
    }

    public List<AccountSettingsEntity> findAllByAccount(ContactingContext context) {
        return jpaRepository.findByAccountId(context.accountId());
    }

    public Optional<AccountSettingsEntity> findById(Long id, ContactingContext context) {
        return jpaRepository.findByIdAndAccountId(id, context.accountId());
    }

    public List<AccountSettingsEntity> getCustomerSettings(Long accountId, String consumer) {
        return jpaRepository.findByAccountIdAndConsumersContaining(accountId, consumer);
    }

    public AccountSettingsEntity saveAndAudit(AccountSettingsEntity entity, ContactingContext context) {
        return super.saveAndAudit(entity, context);
    }

    public AccountSettingsEntity updateAndAudit(AccountSettingsEntity entity, ContactingContext context) {
        return super.updateAndAudit(entity, context);
    }

    public void deleteAndAudit(Long id, ContactingContext context) {
        super.deleteByIdAndAudit(id, context);
    }

    public List<AuditedEntity<AccountSettingsEntity, Long>> getAuditHistory(Long entityId, int limit) {
        return auditActions.getAuditedVersions(entityId, limit);
    }

    public List<Integer> updateAll(List<UpdateSetting> entries) {
        return entries.stream()
                .map(entry ->
                        jpaRepository.updateSettingValue(
                                entry.key(),
                                entry.value(),
                                entry.accountId()
                        ))
                .toList();
    }
}
