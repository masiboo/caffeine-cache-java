package nl.ing.api.contacting.conf.repository;


import nl.ing.api.contacting.conf.domain.entity.cassandra.EmployeesByAccountEntity;
import nl.ing.api.contacting.conf.repository.cassandra.CassandraInMemoryTest;
import nl.ing.api.contacting.conf.repository.cassandra.CassandraTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureCache
class EmployeesByAccountRepositoryTest extends CassandraInMemoryTest {

    @Autowired
    private EmployeesByAccountRepository repository;

    @Test
    @DisplayName("find entity by EmployeeId and AccountFriendlyName should return matching entities")
    void findByEmployeeIdAndAccountFriendlyNameShouldReturnMatchingEntities() {
        // Arrange
        Optional<EmployeesByAccountEntity> entity = CassandraTestData.getEmployeesByAccountEntity();
        repository.save(entity.get());

        // Act
        Optional<EmployeesByAccountEntity> result = repository.findByEmployeeIdAndAccountFriendlyName(
                                                    entity.get().getEmployeeId(),
                                                    entity.get().getAccountFriendlyName());
        // Assert
        assertThat(result).isNotEmpty();
        assertEquals(entity.get().getEmployeeId(), result.get().getEmployeeId());
        assertEquals(entity.get().getAccountFriendlyName(), result.get().getAccountFriendlyName());
    }

    @Test
    @DisplayName("should handle large result sets without timeout")
    void shouldHandleLargeResultSetsWithoutTimeout() {
        // Insert many rows with same account name
        Optional<EmployeesByAccountEntity> entity = CassandraTestData.getEmployeesByAccountEntity();
        EmployeesByAccountEntity employeesByAccountEntity = entity.get();
        for (int i = 0; i < 1000; i++) {
            employeesByAccountEntity.setAccountFriendlyName("large-dataset");
            employeesByAccountEntity.setEmployeeId("employeeId");
            employeesByAccountEntity.setPreferredAccount(true);
            employeesByAccountEntity.setRoles("employeeRole-" + i);
            employeesByAccountEntity.setDepartment("employeeDepartment-" + i);
            employeesByAccountEntity.setTeam("employeeTeam-" + i);
            employeesByAccountEntity.setAllowedChannels(Map.of());
            employeesByAccountEntity.setBusinessUnit("employeeBusinessUnit-" + i);
            employeesByAccountEntity.setWorkerSid("employeeWorkerSid-" + i);
            employeesByAccountEntity.setOrganisationalRestrictions(Set.of(CassandraTestData.getOrganisationalRestriction()));
            repository.save(employeesByAccountEntity);
        }

        // This should complete successfully without timeout
        Optional<EmployeesByAccountEntity> result = repository.findByEmployeeIdAndAccountFriendlyName("employeeId", "large-dataset");
        assertThat(result).isNotEmpty();
        assertEquals(employeesByAccountEntity.getEmployeeId(), result.get().getEmployeeId());
        assertEquals(employeesByAccountEntity.getAccountFriendlyName(), result.get().getAccountFriendlyName());
        }


    @Test
    @DisplayName("should handle special characters in account friendly name")
    void shouldHandleSpecialCharactersInAccountFriendlyName() {
        Optional<EmployeesByAccountEntity> entity = CassandraTestData.getEmployeesByAccountEntity();
        entity.get().setAccountFriendlyName("test@#$%^&*()");
        entity.get().setEmployeeId("test@#$%^&*()");


        EmployeesByAccountEntity savedEntity = repository.save(entity.get());

        Optional<EmployeesByAccountEntity> result = repository.findByEmployeeIdAndAccountFriendlyName(savedEntity.getEmployeeId(),savedEntity.getAccountFriendlyName());
        assertThat(result).isNotEmpty();
        assertEquals(savedEntity.getEmployeeId(), result.get().getEmployeeId());
        assertEquals(savedEntity.getAccountFriendlyName(), result.get().getAccountFriendlyName());
       }

    @Test
    @DisplayName("should handle very long account friendly name")
    void shouldHandleVeryLongAccountFriendlyName() {
        String longName = "a".repeat(1000); // Very long string
        Optional<EmployeesByAccountEntity> entity = CassandraTestData.getEmployeesByAccountEntity();
        entity.get().setAccountFriendlyName(longName);
        entity.get().setEmployeeId(longName);

        EmployeesByAccountEntity savedEntity = repository.save(entity.get());

        Optional<EmployeesByAccountEntity> result = repository.findByEmployeeIdAndAccountFriendlyName(savedEntity.getEmployeeId(), savedEntity.getAccountFriendlyName());
        assertThat(result).isNotEmpty();
        assertEquals(savedEntity.getEmployeeId(), result.get().getEmployeeId());
        assertEquals(savedEntity.getAccountFriendlyName(), result.get().getAccountFriendlyName());

    }

}