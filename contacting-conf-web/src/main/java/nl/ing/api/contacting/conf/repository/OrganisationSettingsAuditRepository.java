package nl.ing.api.contacting.conf.repository;

import com.ing.api.contacting.dto.java.audit.AuditedEntity;
import com.ing.api.contacting.dto.java.context.ContactingContext;
import nl.ing.api.contacting.conf.domain.entity.OrganisationSettingsEntity;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.conf.repository.support.AuditAwareRepository;
import nl.ing.api.contacting.java.repository.jpa.audit.AuditEntityActions;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public class OrganisationSettingsAuditRepository extends AuditAwareRepository<OrganisationSettingsEntity, Long> {

    private final OrganisationAuditRepository organisationRepository;

    private final OrganisationSettingsJpaRepository jpaRepository;

    public OrganisationSettingsAuditRepository(OrganisationSettingsJpaRepository jpaRepository,
                                                 AuditEntityActions<OrganisationSettingsEntity, Long> auditActions,
                                                 OrganisationAuditRepository organisationRepository) {
        super(jpaRepository, auditActions);
        this.jpaRepository = jpaRepository;
        this.organisationRepository = organisationRepository;

    }

    public List<OrganisationSettingsEntity> findByCapabilities(Set<SettingCapability> capabilities, Long accountId) {
        return capabilities.stream()
                .flatMap(capability -> jpaRepository
                        .findByCapabilitiesContainingAndAccountIdOrderByOrgId(capability.value(), accountId)
                        .stream())
                .distinct()
                .toList();

    }

    public List<OrganisationSettingsEntity> findAll(ContactingContext context) {
        return jpaRepository.findAllByOrderByOrgId();
    }

    public Optional<OrganisationSettingsEntity> findById(Long id, ContactingContext context) {
        return jpaRepository.findByIdAndAccountId(id, context.accountId());

    }

    public List<AuditedEntity<OrganisationSettingsEntity, Long>> getAuditHistory(Long accountId, int numRows, ContactingContext context) {
        return auditActions.getAuditedVersions(accountId, numRows);
    }

    public Optional<OrganisationEntity> findByOrganisationId(Long id) {
        return organisationRepository.findById(id);
    }

    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
