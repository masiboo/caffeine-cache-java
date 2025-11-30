package nl.ing.api.contacting.conf.repository;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationLevelEnumeration;
import nl.ing.api.contacting.java.repository.organisation.OrganisationJpaRepository;
import nl.ing.api.contacting.java.repository.jpa.audit.AuditEntityActions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganisationAuditRepositoryTest {

    @Mock
    private OrganisationJpaRepository jpaRepository;
    @Mock
    private AuditEntityActions<OrganisationEntity, Long> auditActions;
    @Mock
    private ContactingContext context;
    private OrganisationAuditRepository auditRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditRepository = new OrganisationAuditRepository(jpaRepository, auditActions);
        when(context.accountId()).thenReturn(42L);
    }

    @Test
    @DisplayName("isSiblingNameUnique returns true when name is unique")
    void isSiblingNameUnique_returnsTrueWhenUnique() {
        when(jpaRepository.existsByNameAndParentIdAndAccountId(anyString(), any(), anyLong())).thenReturn(false);
        boolean result = auditRepository.isSiblingNameUnique("Test", Optional.of(1L), context);
        assertTrue(result);
    }

    @Test
    @DisplayName("isSiblingNameUnique returns false when name is not unique")
    void isSiblingNameUnique_returnsFalseWhenNotUnique() {
        when(jpaRepository.existsByNameAndParentIdAndAccountId(anyString(), any(), anyLong())).thenReturn(true);
        boolean result = auditRepository.isSiblingNameUnique("Test", Optional.of(1L), context);
        assertFalse(result);
    }

    @Test
    @DisplayName("findByName returns entities by name and accountId")
    void findByName_returnsEntities() {
        List<OrganisationEntity> expected = List.of(mock(OrganisationEntity.class));
        when(jpaRepository.findByNameAndAccountId(anyString(), anyLong())).thenReturn(expected);
        List<OrganisationEntity> result = auditRepository.findByName("Test", context);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("findOrgTree returns org tree by accountId")
    void findOrgTree_returnsOrgTree() {
        OrganisationEntity[] arr1 = new OrganisationEntity[]{mock(OrganisationEntity.class)};
        OrganisationEntity[] arr2 = new OrganisationEntity[]{mock(OrganisationEntity.class)};
        List<OrganisationEntity[]> expected = List.of(arr1, arr2);
        when(jpaRepository.findOrgTree(anyLong(), eq(OrganisationLevelEnumeration.SUPER_CIRCLE))).thenReturn(expected);
        List<OrganisationEntity[]> result = auditRepository.findOrgTree(context);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("findOrgById returns org by id and accountId")
    void findOrgById_returnsOrgById() {
        OrganisationEntity[] arr1 = new OrganisationEntity[]{mock(OrganisationEntity.class)};
        OrganisationEntity[] arr2 = new OrganisationEntity[]{mock(OrganisationEntity.class)};
        List<OrganisationEntity[]> expected = List.of(arr1, arr2);
        when(jpaRepository.findByIdAndAccountId(anyLong(), anyLong())).thenReturn(expected);
        List<OrganisationEntity[]> result = auditRepository.findOrgById(1L, context);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("findById delegates to jpaRepository.findById")
    void findById_delegatesToJpaRepository() {
        OrganisationEntity entity = mock(OrganisationEntity.class);
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        Optional<OrganisationEntity> result = auditRepository.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(entity, result.get());
    }

    @Test
    @DisplayName("auditDeleteById calls audit and returns deleted rows")
    void auditDeleteById_callsAuditAndReturnsDeletedRows() {
        when(jpaRepository.deleteByIdAndAccountId(anyLong(), anyLong())).thenReturn(1);
        int result = auditRepository.auditDeleteById(1L, context);
        assertEquals(1, result);
        verify(auditActions).auditDelete(eq(1L), eq(1), eq(context));
    }
}

