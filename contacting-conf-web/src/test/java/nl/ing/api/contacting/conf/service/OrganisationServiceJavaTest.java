package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import nl.ing.api.contacting.conf.entityevents.EntityEventSenderJava;
import nl.ing.api.contacting.conf.repository.OrganisationAuditRepository;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationLevelEnumeration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceJavaTest {

    @Mock
    private OrganisationAuditRepository organisationRepository;
    @Mock
    private ContactingContext context;
    @Mock
    private EntityEventSenderJava entityEventSender;
    private OrganisationServiceJava organisationService;

    private OrganisationEntity parentOfParent;
    private OrganisationEntity parent;
    private OrganisationEntity clt;
    private OrganisationEntity newOrganisation;
    private OrganisationEntity resultOrg;
    private OrganisationEntity withIdOrganisation;
    private OrganisationEntity duplicateNameOrganisation;
    private OrganisationEntity savedOrganisation;

    @BeforeEach
    void setUp() {
        organisationService = new OrganisationServiceJava(organisationRepository, entityEventSender);

        parentOfParent = OrganisationEntity.builder()
                .id(4L)
                .name("parent of parents")
                .accountId(1L)
                .orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE)
                .build();

        parent = OrganisationEntity.builder()
                .id(3L)
                .name("parent of CLT")
                .accountId(1L)
                .parentId(parentOfParent.getId())
                .orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .build();

        clt = OrganisationEntity.builder()
                .id(1L)
                .name("New CLT")
                .accountId(1L)
                .parentId(parent.getId())
                .orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .build();

        newOrganisation = OrganisationEntity.builder()
                .name("New Business Unit")
                .accountId(1L)
                .parentId(3L)
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        resultOrg = OrganisationEntity.builder()
                .id(1L)
                .name("Result Org")
                .accountId(1L)
                .parentId(3L)
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        withIdOrganisation = OrganisationEntity.builder()
                .id(99L)
                .name("Should Fail")
                .accountId(1L)
                .parentId(3L)
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        duplicateNameOrganisation = OrganisationEntity.builder()
                .name("Duplicate Name")
                .accountId(1L)
                .parentId(3L)
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        savedOrganisation = OrganisationEntity.builder()
                .id(100L)
                .name("New Business Unit")
                .accountId(1L)
                .parentId(3L)
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();
    }

    @Nested
    class CreateTests {
        @Test
        @DisplayName("create: success")
        void testCreateSuccess() {
            when(organisationRepository.isSiblingNameUnique(newOrganisation.getName(), Optional.of(3L), context))
                    .thenReturn(true);
            when(organisationRepository.auditSave(newOrganisation, context))
                    .thenReturn(savedOrganisation);

            var result = organisationService.create(newOrganisation, context);
            assertInstanceOf(Long.class, result);
            assertEquals(savedOrganisation.getId(), result);
        }

        @Test
        @DisplayName("create: id already set")
        void testCreateWithId() {
            var ex = assertThrows(RuntimeException.class, () -> organisationService.create(withIdOrganisation, context));
            assertTrue(ex.getMessage().contains("Identifier should not be set"));
        }

        @Test
        @DisplayName("create: non-unique name")
        void testCreateNonUniqueName() {
            when(organisationRepository.isSiblingNameUnique(duplicateNameOrganisation.getName(), Optional.of(3L), context))
                    .thenReturn(false);

            var ex = assertThrows(RuntimeException.class, () -> organisationService.create(duplicateNameOrganisation, context));
            assertTrue(ex.getMessage().contains("Name is not unique"));
        }
    }

    @Nested
    class UpdateTests {


        @Test
        @DisplayName("update: null id")
        void testUpdateNullId() {
            var orgNoId = OrganisationEntity.builder()
                    .name("NoId")
                    .accountId(1L)
                    .orgLevel(OrganisationLevelEnumeration.CLT)
                    .build();

            var ex = assertThrows(RuntimeException.class, () -> organisationService.update(orgNoId, context));
            assertTrue(ex.getMessage().contains("Identifier should be set"));
        }

        @Test
        @DisplayName("update: non-unique name")
        void testUpdateNonUniqueName() {
            when(organisationRepository.isSiblingNameUnique(clt.getName(), Optional.of(parent.getId()), context))
                    .thenReturn(false);

            var ex = assertThrows(RuntimeException.class, () -> organisationService.update(clt, context));
            assertTrue(ex.getMessage().contains("Name is not unique"));
        }
    }



        @Test
        @DisplayName("getById: should return empty when not found")
        void getById_returnsEmpty() {

            var result = organisationService.getById(99L, context);
            assertTrue(result.isEmpty());
        }
    }
