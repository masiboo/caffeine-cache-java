package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.business.kafka.entityevents.ContactingEntityKafkaEventDataSyntaxJava;
import nl.ing.api.contacting.conf.entityevents.EntityEventSenderJava;
import nl.ing.api.contacting.conf.exception.Errors;

import nl.ing.api.contacting.conf.mapper.OrganisationMapperJava;
import nl.ing.api.contacting.conf.repository.OrganisationAuditRepository;

import nl.ing.api.contacting.conf.util.OrganisationFilter;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import nl.ing.api.contacting.java.domain.OrganisationVO;
import nl.ing.api.contacting.trust.rest.context.SessionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganisationServiceJava {

    private final OrganisationAuditRepository organisationRepository;
    private final EntityEventSenderJava entityEventSender;

    @Transactional(readOnly = true)
    public List<OrganisationVO> getOrganisationTree(ContactingContext context) {
        return OrganisationMapperJava.organisationHierarchyToVo(organisationRepository.findOrgTree(context));
    }

    @Transactional(readOnly = true)
    public Optional<OrganisationVO> getById(Long id, ContactingContext context) {
        return OrganisationMapperJava.organisationSubtreeById(id, organisationRepository.findOrgById(id, context));
    }

    @Transactional
    public Long create(OrganisationEntity organisationModel, ContactingContext context) {
        if (organisationModel.getId() != null) {
            throw Errors.badRequest("requirement failed: Identifier should not be set while creating an organisation");
        }

        if (!isUniqueName(organisationModel, context)) {
            throw Errors.badRequest("requirement failed: Name is not unique");
        }

        OrganisationEntity saved = organisationRepository.auditSave(organisationModel, context);
        return saved.getId();
    }

    @Transactional
    public int delete(Long id, ContactingContext context) {
        Optional<OrganisationVO> orgOpt = getById(id, context);
        if (orgOpt.isEmpty()) {
            throw Errors.notFound("Organisation with id " + id + " not found");
        }
        try {
            int deletedRows = organisationRepository.auditDeleteById(id, context);
            if (deletedRows > 0) {
                entityEventSender.sendEntityEvent(ContactingEntityKafkaEventDataSyntaxJava.toEventDeleteData(orgOpt.get(), context.accountId(), Optional.of(orgOpt.get().name())))
                        .exceptionally(ex -> {
                            log.warn("Unable to send org entity delete message to kafka", ex);
                            return null;
                        });
            }
            return deletedRows;
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            Throwable root = ex.getCause();
            while (root != null && root.getCause() != null) {
                root = root.getCause();
            }
            String msg = root != null ? root.getMessage() : ex.getMessage();
            throw Errors.badRequest("sql exception - " + msg);
        } catch (Exception ex) {
            log.error("Unexpected error during organisation delete", ex);
            throw Errors.unexpected("Unexpected error during organisation delete");
        }
    }

    @Transactional
    public OrganisationEntity update(OrganisationEntity organisationEntity, ContactingContext context) {
        if (organisationEntity.getId() == null) {
            throw Errors.badRequest("requirement failed: Identifier should be set while update an organisation");
        }

        if (!isUniqueName(organisationEntity, context)) {
            throw Errors.badRequest("requirement failed: Name is not unique");
        }

        return organisationRepository.auditUpdate(organisationEntity, context);
    }

    private boolean isUniqueName(OrganisationEntity org, ContactingContext context) {
        Optional<Long> parentId = Optional.ofNullable(org.getParentId());
        return organisationRepository.isSiblingNameUnique(org.getName(), parentId, context);
    }

    public List<OrganisationVO> getAllowedOrganisations(Optional<SessionContext> sessionContext,
                                                            ContactingContext contactingContext) {
        if(sessionContext.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrganisationVO> organisations = getOrganisationTree(contactingContext);
        return OrganisationFilter.getAllowedOrganisations(sessionContext.get(), organisations);
    }
}
