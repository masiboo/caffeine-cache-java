package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.OrganisationSettingsEntity;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import nl.ing.api.contacting.java.repository.model.organisation.OrganisationLevelEnumeration;

@DataJpaTest
@ActiveProfiles("test")
class OrganisationSettingsJpaRepositoryIntegrationTest {

    @Autowired
    private OrganisationSettingsJpaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private OrganisationEntity persistOrganisation(Long id) {
        OrganisationEntity organisation = OrganisationEntity.builder()
                .accountId(id)
                .name("Test Org " + id)
                .orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .build();
        return entityManager.persistAndFlush(organisation);
    }

    private OrganisationSettingsEntity buildSettings(OrganisationEntity organisation, String key, String value, Long accountId, String capabilities, boolean enabled) {
        return OrganisationSettingsEntity.builder()
                .orgId(organisation.getId())
                .key(key)
                .value(value)
                .accountId(accountId)
                .capabilities(capabilities)
                .enabled(enabled)
                .build();
    }

    @Nested
    @DisplayName("findByCapabilitiesContainingAndAccountIdOrderByOrgId")
    class FindByCapabilitiesAndAccountId {

        @Test
        @DisplayName("should return settings for given capabilities and account")
        void shouldReturnSettingsForGivenCapabilitiesAndAccount() {

            OrganisationEntity org = persistOrganisation(1L);
            OrganisationSettingsEntity settings = buildSettings(org, "key1", "value1", 1L, "chat", true);
            entityManager.persistAndFlush(settings);

            List<OrganisationSettingsEntity> result = repository.findByCapabilitiesContainingAndAccountIdOrderByOrgId("chat", 1L);

            assertEquals(1, result.size());
            assertEquals("chat", result.get(0).getCapabilities());
            assertEquals(1L, result.get(0).getAccountId());
        }

        @Test
        @DisplayName("should return empty list when no settings found")
        void shouldReturnEmptyListWhenNoSettingsFound() {
            List<OrganisationSettingsEntity> result = repository.findByCapabilitiesContainingAndAccountIdOrderByOrgId("nonexistent", 99L);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return multiple settings ordered by orgId")
        void shouldReturnMultipleSettingsOrderedByOrgId() {
            OrganisationEntity org1 = persistOrganisation(1L);
            OrganisationEntity org2 = persistOrganisation(2L);

            OrganisationSettingsEntity s1 = buildSettings(org1, "keyA", "valA", 1L, "chat", true);
            OrganisationSettingsEntity s2 = buildSettings(org2, "keyB", "valB", 1L, "chat", true);

            entityManager.persist(s2); // org2 has higher id, will be ordered after org1
            entityManager.persist(s1);
            entityManager.flush();

            List<OrganisationSettingsEntity> result = repository.findByCapabilitiesContainingAndAccountIdOrderByOrgId("chat", 1L);

            assertEquals(2, result.size());
            assertTrue(result.get(0).getOrgId() < result.get(1).getOrgId());
        }
    }

    @Nested
    @DisplayName("findByOrgIdInAndAccountId")
    class FindByOrganisationIds {

        @Test
        @DisplayName("should return settings for given organisation ids")
        void shouldReturnSettingsForGivenOrganisationIds() {
            OrganisationEntity org1 = persistOrganisation(1L);
            OrganisationEntity org2 = persistOrganisation(2L);

            OrganisationSettingsEntity s1 = buildSettings(org1, "key1", "val1", 1L, "chat", true);
            OrganisationSettingsEntity s2 = buildSettings(org2, "key2", "val2", 1L, "chat", true);

            entityManager.persist(s1);
            entityManager.persist(s2);
            entityManager.flush();

            Set<Long> orgIds = Set.of(org1.getId(), org2.getId());
            List<OrganisationSettingsEntity> result = repository.findByOrgIdInAndAccountId(orgIds, 1L);

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(e -> orgIds.contains(e.getOrgId())));
        }

        @Test
        @DisplayName("should return empty list when no settings found for organisation ids")
        void shouldReturnEmptyListWhenNoSettingsFoundForOrganisationIds() {
            Set<Long> orgIds = Set.of(999L, 888L);
            List<OrganisationSettingsEntity> result = repository.findByOrgIdInAndAccountId(orgIds, 1L);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should filter by account id correctly")
        void shouldFilterByAccountIdCorrectly() {
            OrganisationEntity org = persistOrganisation(1L);

            OrganisationSettingsEntity s1 = buildSettings(org, "key1", "val1", 1L, "chat", true);
            OrganisationSettingsEntity s2 = buildSettings(org, "key2", "val2", 2L, "chat", true);

            entityManager.persist(s1);
            entityManager.persist(s2);
            entityManager.flush();

            Set<Long> orgIds = Set.of(org.getId());
            List<OrganisationSettingsEntity> result = repository.findByOrgIdInAndAccountId(orgIds, 1L);

            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getAccountId());
        }
    }

    @Nested
    @DisplayName("JpaRepository methods")
    class JpaRepositoryMethods {

        @Test
        @DisplayName("should save organisation setting")
        void shouldSaveOrganisationSetting() {
            OrganisationEntity org = persistOrganisation(1L);
            OrganisationSettingsEntity settings = buildSettings(org, "saveKey", "saveValue", 1L, "chat", true);

            OrganisationSettingsEntity saved = repository.save(settings);

            assertNotNull(saved.getId());
            assertEquals("saveKey", saved.getKey());
            assertEquals(org.getId(), saved.getOrgId());
        }

        @Test
        @DisplayName("should find organisation setting by id")
        void shouldFindOrganisationSettingById() {
            OrganisationEntity org = persistOrganisation(1L);
            OrganisationSettingsEntity settings = buildSettings(org, "findKey", "findValue", 1L, "chat", true);

            OrganisationSettingsEntity saved = entityManager.persistAndFlush(settings);

            Optional<OrganisationSettingsEntity> found = repository.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals("findKey", found.get().getKey());
        }

        @Test
        @DisplayName("should return empty optional when organisation setting not found")
        void shouldReturnEmptyOptionalWhenOrganisationSettingNotFound() {
            Optional<OrganisationSettingsEntity> found = repository.findById(999L);
            assertTrue(found.isEmpty());
        }

        @Test
        @DisplayName("should delete organisation setting")
        void shouldDeleteOrganisationSetting() {
            OrganisationEntity org = persistOrganisation(1L);
            OrganisationSettingsEntity settings = buildSettings(org, "delKey", "delValue", 1L, "chat", true);

            OrganisationSettingsEntity saved = entityManager.persistAndFlush(settings);

            repository.deleteById(saved.getId());
            entityManager.flush();

            Optional<OrganisationSettingsEntity> found = repository.findById(saved.getId());
            assertTrue(found.isEmpty());
        }
    }
}
