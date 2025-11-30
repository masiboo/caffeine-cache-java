package nl.ing.api.contacting.conf.repository;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.repository.support.AuditAwareRepository;
import nl.ing.api.contacting.java.repository.jpa.audit.AuditEntityActions;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationLevelEnumeration;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import nl.ing.api.contacting.java.repository.organisation.OrganisationJpaRepository;

@Repository
@Slf4j
public class OrganisationAuditRepository extends AuditAwareRepository<OrganisationEntity, Long> {
    private final OrganisationJpaRepository jpaRepository;


    protected OrganisationAuditRepository(OrganisationJpaRepository jpaRepository, AuditEntityActions<OrganisationEntity, Long> auditActions) {
        super(jpaRepository, auditActions);

        this.jpaRepository = jpaRepository;
    }

    public boolean isSiblingNameUnique(String name, Optional<Long> parentId, ContactingContext context) {
        return !jpaRepository.existsByNameAndParentIdAndAccountId(
                name.toLowerCase(),
                parentId.orElse(null),
                context.accountId()
        );
    }

    public List<OrganisationEntity> findByName(String name, ContactingContext context) {
        return jpaRepository.findByNameAndAccountId(name, context.accountId());
    }

    public List<OrganisationEntity[]> findOrgTree(ContactingContext context) {
        return jpaRepository.findOrgTree(context.accountId(), OrganisationLevelEnumeration.SUPER_CIRCLE);
    }

    public List<OrganisationEntity[]> findOrgById(Long id, ContactingContext context) {
        return jpaRepository.findByIdAndAccountId(id, context.accountId());
    }


    public OrganisationEntity auditUpdate(OrganisationEntity entity, ContactingContext context) {
        return super.updateAndAudit(entity, context);
    }


    public OrganisationEntity auditSave(OrganisationEntity entity, ContactingContext context) {
        return super.saveAndAudit(entity, context);
    }
    // Added method to find by ID for the use of OrganisationSettings flow
    public Optional<OrganisationEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    public int auditDeleteById(Long id, ContactingContext context) {
        int deletedRows = jpaRepository.deleteByIdAndAccountId(id,context.accountId());
        auditActions.auditDelete(id, deletedRows,  context);
        return deletedRows;
    }

}
