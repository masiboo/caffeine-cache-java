package nl.ing.api.contacting.conf.repository;


import nl.ing.api.contacting.conf.domain.entity.cassandra.ContactingConfigEntity;
import nl.ing.api.contacting.conf.repository.cassandra.CassandraInMemoryTest;
import nl.ing.api.contacting.conf.repository.cassandra.CassandraTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureCache
class ContactingConfigRepositoryTest extends CassandraInMemoryTest {

    @Autowired
    private ContactingConfigRepository repository;

    @Test
    @DisplayName("find entity by key return matching entities")
    void findByAccountFriendlyNameShouldReturnMatchingEntities() {
        // Arrange
        ContactingConfigEntity entity = CassandraTestData.getContactingConfigEntity();
        ContactingConfigEntity saveEntity = repository.save(entity);

        // Act
        List<ContactingConfigEntity> entities = repository.findByKey(saveEntity.getKey());

        // Assert
        assertThat(entities).isNotEmpty();
        assertEquals(1, entities.size());
        assertThat(entities.get(0).getKey()).isEqualTo(entity.getKey());
    }


    @Test
    @DisplayName("should handle special characters in account friendly name")
    void shouldHandleSpecialCharactersInAccountFriendlyName() {
        ContactingConfigEntity entity = CassandraTestData.getContactingConfigEntity();
        entity.setKey("test@#$%^&*()");
        entity.setValues("test@#$%^&*()");


        ContactingConfigEntity savedEntity = repository.save(entity);

        List<ContactingConfigEntity> result = repository.findByKey(savedEntity.getKey());

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("should handle very long account friendly name")
    void shouldHandleVeryLongAccountFriendlyName() {
        String longValue = "a".repeat(1000); // Very long string
        ContactingConfigEntity entity = CassandraTestData.getContactingConfigEntity();
        entity.setKey(longValue);
        entity.setValues(longValue);


        ContactingConfigEntity savedEntity = repository.save(entity);

        List<ContactingConfigEntity> result = repository.findByKey(savedEntity.getKey());
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
    }
}