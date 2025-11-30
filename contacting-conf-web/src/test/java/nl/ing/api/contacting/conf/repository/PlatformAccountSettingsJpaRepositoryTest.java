package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.PlatformAccountSettingsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PlatformAccountSettingsJpaRepository Tests")
class PlatformAccountSettingsJpaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlatformAccountSettingsJpaRepository platformAccountSettingsJpaRepository;

    private PlatformAccountSettingsEntity testEntity1;

    @BeforeEach
    void setUp() {
        testEntity1 = PlatformAccountSettingsEntity.builder()
                .key("platform.theme")
                .value("dark")
                .accountId(1L)
                .build();

        PlatformAccountSettingsEntity testEntity2 = PlatformAccountSettingsEntity.builder()
                .key("platform.language")
                .value("en")
                .accountId(1L)
                .build();

        PlatformAccountSettingsEntity testEntity3 = PlatformAccountSettingsEntity.builder()
                .key("platform.timezone")
                .value("UTC")
                .accountId(2L)
                .build();

        entityManager.persistAndFlush(testEntity1);
        entityManager.persistAndFlush(testEntity2);
        entityManager.persistAndFlush(testEntity3);
    }

    @Test
    @DisplayName("should find all settings by account ID")
    void shouldFindByAccountId() {
        List<PlatformAccountSettingsEntity> result = platformAccountSettingsJpaRepository.findByAccountId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PlatformAccountSettingsEntity::getAccountId)
                .containsOnly(1L);
        assertThat(result).extracting(PlatformAccountSettingsEntity::getKey)
                .containsExactlyInAnyOrder("platform.theme", "platform.language");
    }

    @Test
    @DisplayName("should find setting by ID when it exists")
    void shouldFindByIdWhenExists() {
        Optional<PlatformAccountSettingsEntity> result = platformAccountSettingsJpaRepository
                .findById(testEntity1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getKey()).isEqualTo("platform.theme");
        assertThat(result.get().getValue()).isEqualTo("dark");
    }

    @Test
    @DisplayName("should return empty when finding by non-existing ID")
    void shouldReturnEmptyWhenFindingByNonExistingId() {
        Optional<PlatformAccountSettingsEntity> result = platformAccountSettingsJpaRepository
                .findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should find settings by account ID and verify content")
    void shouldFindByAccountIdAndVerifyContent() {
        List<PlatformAccountSettingsEntity> result = platformAccountSettingsJpaRepository
                .findByAccountId(1L);

        Optional<PlatformAccountSettingsEntity> themeEntity = result.stream()
                .filter(entity -> "platform.theme".equals(entity.getKey()))
                .findFirst();

        assertThat(themeEntity).isPresent();
        assertThat(themeEntity.get().getKey()).isEqualTo("platform.theme");
        assertThat(themeEntity.get().getValue()).isEqualTo("dark");
    }

    @Test
    @DisplayName("should return empty list when finding by non-existing account")
    void shouldReturnEmptyListWhenFindingByNonExistingAccount() {
        List<PlatformAccountSettingsEntity> result = platformAccountSettingsJpaRepository
                .findByAccountId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should save and retrieve new entity")
    void shouldSaveAndRetrieveNewEntity() {
        PlatformAccountSettingsEntity newEntity = PlatformAccountSettingsEntity.builder()
                .key("platform.currency")
                .value("USD")
                .accountId(3L)
                .build();

        PlatformAccountSettingsEntity saved = platformAccountSettingsJpaRepository.save(newEntity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getKey()).isEqualTo("platform.currency");

        Optional<PlatformAccountSettingsEntity> retrieved = platformAccountSettingsJpaRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getValue()).isEqualTo("USD");
    }

    @Test
    @DisplayName("should update entity using update method")
    void shouldUpdateEntityUsingUpdateMethod() {
        testEntity1.update("platform.theme", "light");
        PlatformAccountSettingsEntity updated = platformAccountSettingsJpaRepository.save(testEntity1);

        assertThat(updated.getKey()).isEqualTo("platform.theme");
        assertThat(updated.getValue()).isEqualTo("light");
    }

    @Test
    @DisplayName("should delete entity by ID")
    void shouldDeleteEntityById() {
        Long entityId = testEntity1.getId();

        platformAccountSettingsJpaRepository.deleteById(entityId);

        Optional<PlatformAccountSettingsEntity> deleted = platformAccountSettingsJpaRepository.findById(entityId);
        assertThat(deleted).isEmpty();
    }
}
