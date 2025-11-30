package nl.ing.api.contacting.conf.service;

import nl.ing.api.contacting.conf.domain.entity.cassandra.ContactingConfigEntity;
import nl.ing.api.contacting.conf.repository.ContactingConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactingConfigServiceTest {

    @Mock
    private ContactingConfigRepository contactingConfigRepository;

    private ContactingConfigService contactingConfigService;

    private ContactingConfigEntity configEntity1;
    private ContactingConfigEntity configEntity2;
    private List<ContactingConfigEntity> allConfigEntities;

    @BeforeEach
    void setUp() {
        contactingConfigService = new ContactingConfigService(contactingConfigRepository);

        configEntity1 = getContactingConfigEntity();
        configEntity2 = getContactingConfigEntity();

        allConfigEntities = Arrays.asList(configEntity1, configEntity2);
    }

    @Nested
    class FindByKeyTests {

        @Test
        @DisplayName("findByKey: success with multiple entities")
        void testFindByKeySuccess() {
            String testKey = "test.key";
            when(contactingConfigRepository.findByKey(testKey))
                    .thenReturn(Arrays.asList(configEntity1, configEntity2));

            Set<String> result = contactingConfigService.findByKey(testKey);

            assertNotNull(result);
            assertInstanceOf(Set.class, result);
        }

        @Test
        @DisplayName("findByKey: returns empty set when no entities found")
        void testFindByKeyEmpty() {
            String testKey = "nonexistent.key";
            when(contactingConfigRepository.findByKey(testKey))
                    .thenReturn(Collections.emptyList());

            Set<String> result = contactingConfigService.findByKey(testKey);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class FindAllTests {

        @Test
        @DisplayName("findAll: success")
        void testFindAllSuccess() {
            when(contactingConfigRepository.findAll())
                    .thenReturn(allConfigEntities);

            List<ContactingConfigEntity> result = contactingConfigService.findAll();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(allConfigEntities, result);
        }

        @Test
        @DisplayName("findAll: returns empty list when no entities")
        void testFindAllEmpty() {
            when(contactingConfigRepository.findAll())
                    .thenReturn(Collections.emptyList());

            List<ContactingConfigEntity> result = contactingConfigService.findAll();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    private ContactingConfigEntity  getContactingConfigEntity(){

        return ContactingConfigEntity.builder()
                .key("key1")
                .values("value1")
                .build();
    }

}