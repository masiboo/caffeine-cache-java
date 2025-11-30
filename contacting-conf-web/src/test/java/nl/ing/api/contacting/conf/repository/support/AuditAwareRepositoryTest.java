package nl.ing.api.contacting.conf.repository.support;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import nl.ing.api.contacting.java.repository.jpa.audit.AuditEntityActions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditAwareRepository Tests")
class AuditAwareRepositoryTest {

    @Mock
    private JpaRepository<TestEntity, Long> jpaRepository;

    @Mock
    private AuditEntityActions<TestEntity, Long> auditActions;

    @Mock
    private ContactingContext contactingContext;

    private TestAuditAwareRepository testRepository;

    // Test entity for testing purposes
    private static class TestEntity {
        private final String name;

        public TestEntity(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    // Test implementation of abstract class
    private static class TestAuditAwareRepository extends AuditAwareRepository<TestEntity, Long> {
        protected TestAuditAwareRepository(JpaRepository<TestEntity, Long> jpaRepository,
                                         AuditEntityActions<TestEntity, Long> auditActions) {
            super(jpaRepository, auditActions);
        }
    }

    @BeforeEach
    void setUp() {
        testRepository = new TestAuditAwareRepository(jpaRepository, auditActions);
    }

    @Test
    @DisplayName("should save entity and audit insert")
    void shouldSaveAndAudit() {
        TestEntity entity = new TestEntity("test-entity");
        TestEntity savedEntity = new TestEntity("saved-entity");

        when(jpaRepository.save(entity)).thenReturn(savedEntity);

        TestEntity result = testRepository.saveAndAudit(entity, contactingContext);

        assertThat(result).isEqualTo(savedEntity);
        verify(jpaRepository).save(entity);
        verify(auditActions).auditInsert(savedEntity, contactingContext);
    }

    @Test
    @DisplayName("should update entity and audit update")
    void shouldUpdateAndAudit() {
        TestEntity entity = new TestEntity("test-entity");
        TestEntity updatedEntity = new TestEntity("updated-entity");

        when(jpaRepository.save(entity)).thenReturn(updatedEntity);

        TestEntity result = testRepository.updateAndAudit(entity, contactingContext);

        assertThat(result).isEqualTo(updatedEntity);
        verify(jpaRepository).save(entity);
        verify(auditActions).auditUpdate(updatedEntity, contactingContext);
    }

    @Test
    @DisplayName("should delete entity by ID and audit delete")
    void shouldDeleteByIdAndAudit() {
        Long entityId = 123L;

        testRepository.deleteByIdAndAudit(entityId, contactingContext);

        verify(jpaRepository).deleteById(entityId);
        verify(auditActions).auditDelete(entityId, 1, contactingContext);
    }

    @Test
    @DisplayName("should handle repository operations in correct order")
    void shouldHandleRepositoryOperationsInCorrectOrder() {
        TestEntity entity = new TestEntity("test-entity");

        when(jpaRepository.save(entity)).thenReturn(entity);

        testRepository.saveAndAudit(entity, contactingContext);

        // Verify order of operations
        var inOrder = inOrder(jpaRepository, auditActions);
        inOrder.verify(jpaRepository).save(entity);
        inOrder.verify(auditActions).auditInsert(entity, contactingContext);
    }

    @Test
    @DisplayName("should handle update operations in correct order")
    void shouldHandleUpdateOperationsInCorrectOrder() {
        TestEntity entity = new TestEntity("test-entity");

        when(jpaRepository.save(entity)).thenReturn(entity);

        testRepository.updateAndAudit(entity, contactingContext);

        // Verify order of operations
        var inOrder = inOrder(jpaRepository, auditActions);
        inOrder.verify(jpaRepository).save(entity);
        inOrder.verify(auditActions).auditUpdate(entity, contactingContext);
    }

    @Test
    @DisplayName("should handle delete operations in correct order")
    void shouldHandleDeleteOperationsInCorrectOrder() {
        Long entityId = 456L;

        testRepository.deleteByIdAndAudit(entityId, contactingContext);

        // Verify order of operations
        var inOrder = inOrder(jpaRepository, auditActions);
        inOrder.verify(jpaRepository).deleteById(entityId);
        inOrder.verify(auditActions).auditDelete(entityId, 1, contactingContext);
    }

    @Test
    @DisplayName("should propagate save exceptions")
    void shouldPropagateSaveExceptions() {
        TestEntity entity = new TestEntity("test-entity");
        RuntimeException expectedException = new RuntimeException("Save failed");

        when(jpaRepository.save(entity)).thenThrow(expectedException);

        try {
            testRepository.saveAndAudit(entity, contactingContext);
        } catch (RuntimeException ex) {
            assertThat(ex).isEqualTo(expectedException);
        }

        verify(jpaRepository).save(entity);
        verifyNoInteractions(auditActions);
    }

    @Test
    @DisplayName("should propagate delete exceptions")
    void shouldPropagateDeleteExceptions() {
        Long entityId = 789L;
        RuntimeException expectedException = new RuntimeException("Delete failed");

        doThrow(expectedException).when(jpaRepository).deleteById(entityId);

        try {
            testRepository.deleteByIdAndAudit(entityId, contactingContext);
        } catch (RuntimeException ex) {
            assertThat(ex).isEqualTo(expectedException);
        }

        verify(jpaRepository).deleteById(entityId);
        verifyNoInteractions(auditActions);
    }
}

