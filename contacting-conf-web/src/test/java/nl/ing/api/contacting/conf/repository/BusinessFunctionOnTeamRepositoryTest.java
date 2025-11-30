package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.cassandra.BusinessFunctionOnTeamEntity;
import nl.ing.api.contacting.conf.repository.cassandra.CassandraInMemoryTest;
import nl.ing.api.contacting.conf.repository.cassandra.CassandraTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AutoConfigureCache
class BusinessFunctionOnTeamRepositoryTest extends CassandraInMemoryTest {

    @Autowired
    private BusinessFunctionOnTeamRepository repository;

    @Test
    @DisplayName("find by AccountFriendlyName should return matching entities")
    void findByAccountFriendlyNameShouldReturnMatchingEntities() {
        // Arrange
        BusinessFunctionOnTeamEntity entity = CassandraTestData.getBusinessFunctionOnTeamEntity();
        BusinessFunctionOnTeamEntity saveEntity = repository.save(entity);

        // Act
        List<BusinessFunctionOnTeamEntity> result = repository.findByAccountFriendlyName(saveEntity.getAccountFriendlyName());

        // Assert
        assertThat(result).isNotEmpty();
        assertEquals(1, result.size());
        assertThat(result.get(0).getAccountFriendlyName()).isEqualTo(entity.getAccountFriendlyName());
    }

    @Test
    @DisplayName("delete entity by AccountFriendlyName And BusinessFunction And OrganisationId And Role should delete matching entity")
    void deleteByAccountFriendlyNameAndBusinessFunctionAndOrganisationIdAndRoleShouldDeleteMatchingEntities() {
        // Arrange
        BusinessFunctionOnTeamEntity entity = CassandraTestData.getBusinessFunctionOnTeamEntity();
        BusinessFunctionOnTeamEntity saveEntity = repository.save(entity);

        List<BusinessFunctionOnTeamEntity> result = repository.findByAccountFriendlyName(entity.getAccountFriendlyName());
        assertThat(result).isNotEmpty();
        assertEquals(1, result.size());
        assertThat(result.get(0).getAccountFriendlyName()).isEqualTo(entity.getAccountFriendlyName());

        // Act
        repository.deleteByAccountFriendlyNameAndBusinessFunctionAndOrganisationIdAndRole(
                entity.getAccountFriendlyName(), saveEntity.getBusinessFunction(), entity.getOrganisationId(), entity.getRole());

        List<BusinessFunctionOnTeamEntity> resultAfterDelete =  repository.findByAccountFriendlyName(entity.getAccountFriendlyName());
        assertThat(resultAfterDelete).isEmpty();
        assertEquals(0, resultAfterDelete.size());
    }

    @Test
    @DisplayName("should handle large result sets without timeout")
    void shouldHandleLargeResultSetsWithoutTimeout() {
        // Insert many rows with same account name
        for (int i = 0; i < 1000; i++) {
            BusinessFunctionOnTeamEntity entity = CassandraTestData.getBusinessFunctionOnTeamEntity();
            entity.setAccountFriendlyName("large-dataset");
            entity.setBusinessFunction("function-" + i);
            entity.setOrganisationId(i);
            entity.setRole("role-" + i);
            repository.save(entity);
        }

        // This should complete successfully without timeout
        List<BusinessFunctionOnTeamEntity> result = repository.findByAccountFriendlyName("large-dataset");

        assertThat(result).hasSize(1000);
    }

    @Test
    @DisplayName("should handle null account friendly name gracefully")
    void shouldHandleNullAccountFriendlyNameGracefully() {
        assertThrows(Exception.class, () ->
                repository.findByAccountFriendlyName(null)
        );
    }

    @Test
    @DisplayName("delete should fail silently when entity does not exist")
    void deleteShouldFailSilentlyWhenEntityDoesNotExist() {
        // This should not throw an exception even when the entity doesn't exist
        repository.deleteByAccountFriendlyNameAndBusinessFunctionAndOrganisationIdAndRole(
                "non-existent-account",
                "non-existent-function",
                999,
                "non-existent-role"
        );

        // Verify no entities exist for this account
        List<BusinessFunctionOnTeamEntity> result = repository.findByAccountFriendlyName("non-existent-account");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should handle special characters in account friendly name")
    void shouldHandleSpecialCharactersInAccountFriendlyName() {
        BusinessFunctionOnTeamEntity entity = CassandraTestData.getBusinessFunctionOnTeamEntity();
        entity.setAccountFriendlyName("test@#$%^&*()");

        BusinessFunctionOnTeamEntity savedEntity = repository.save(entity);

        List<BusinessFunctionOnTeamEntity> result = repository.findByAccountFriendlyName("test@#$%^&*()");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountFriendlyName()).isEqualTo(savedEntity.getAccountFriendlyName());
    }

    @Test
    @DisplayName("should handle very long account friendly name")
    void shouldHandleVeryLongAccountFriendlyName() {
        String longName = "a".repeat(1000); // Very long string
        BusinessFunctionOnTeamEntity entity = CassandraTestData.getBusinessFunctionOnTeamEntity();
        entity.setAccountFriendlyName(longName);

        BusinessFunctionOnTeamEntity savedEntity = repository.save(entity);

        List<BusinessFunctionOnTeamEntity> result = repository.findByAccountFriendlyName(longName);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountFriendlyName()).isEqualTo(savedEntity.getAccountFriendlyName());
    }

}
