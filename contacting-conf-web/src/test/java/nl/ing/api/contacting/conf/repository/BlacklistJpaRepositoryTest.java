package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.AccountSettingsEntity;
import nl.ing.api.contacting.conf.domain.entity.BlacklistEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BlacklistJpaRepositoryTest")
class BlacklistJpaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BlacklistJpaRepository blacklistJpaRepository;

    private AccountSettingsEntity account1;
    private AccountSettingsEntity account2;
    private BlacklistEntity activeBlacklist1;
    private BlacklistEntity activeBlacklist2;

    @BeforeEach
    void setUp() {
        // Create AccountSettingsEntity with all required fields
        account1 = AccountSettingsEntity.builder()
                .accountId(1L)
                .key("test-key-1")
                .value("test-value-1")
                .capabilities("test-capability-1")
                .consumers("test-consumers-1")
                .build();

        account2 = AccountSettingsEntity.builder()
                .accountId(2L)
                .key("test-key-2")
                .value("test-value-2")
                .capabilities("test-capability-2")
                .consumers("test-consumers-2")
                .build();

        // Persist and get the managed entities with generated IDs
        account1 = entityManager.persistAndFlush(account1);
        account2 = entityManager.persistAndFlush(account2);

        activeBlacklist1 = new BlacklistEntity();
        activeBlacklist1.setFunctionality("TRANSFER");
        activeBlacklist1.setEntityType("USER");
        activeBlacklist1.setValue("user-1");
        activeBlacklist1.setStartDate(LocalDateTime.now().minusDays(1));
        activeBlacklist1.setEndDate(LocalDateTime.now().plusDays(1));
        activeBlacklist1.setAccountId(account1.getId());
        activeBlacklist1.setAccount(account1);

        BlacklistEntity expiredBlacklist = new BlacklistEntity();
        expiredBlacklist.setFunctionality("TRANSFER");
        expiredBlacklist.setEntityType("USER");
        expiredBlacklist.setValue("user-2");
        expiredBlacklist.setStartDate(LocalDateTime.now().minusDays(5));
        expiredBlacklist.setEndDate(LocalDateTime.now().minusDays(1));
        expiredBlacklist.setAccountId(account1.getId());
        expiredBlacklist.setAccount(account1);

        activeBlacklist2 = new BlacklistEntity();
        activeBlacklist2.setFunctionality("PAYMENT");
        activeBlacklist2.setEntityType("USER");
        activeBlacklist2.setValue("user-3");
        activeBlacklist2.setStartDate(LocalDateTime.now().minusDays(2));
        activeBlacklist2.setEndDate(null);
        activeBlacklist2.setAccountId(account2.getId());
        activeBlacklist2.setAccount(account2);

        entityManager.persistAndFlush(activeBlacklist1);
        entityManager.persistAndFlush(expiredBlacklist);
        entityManager.persistAndFlush(activeBlacklist2);
    }

    @Test
    @DisplayName("findActiveByAccount should return only active blacklists for account")
    void findActiveByAccount_shouldReturnActiveBlacklists() {
        List<BlacklistEntity> result = blacklistJpaRepository.findActiveByAccount(account1.getId(), LocalDateTime.now());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValue()).isEqualTo("user-1");

        List<BlacklistEntity> result2 = blacklistJpaRepository.findActiveByAccount(account2.getId(), LocalDateTime.now());
        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getValue()).isEqualTo("user-3");
    }

    @Test
    @DisplayName("findActiveByAccount should return empty list for account with no active blacklists")
    void findActiveByAccount_shouldReturnEmptyForNoActive() {
        List<BlacklistEntity> result = blacklistJpaRepository.findActiveByAccount(999L, LocalDateTime.now());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByAccountIdAndFunctionalityAndActive should filter by functionality and active status")
    void findByAccountIdAndFunctionalityAndActive_shouldFilterCorrectly() {
        List<BlacklistEntity> result = blacklistJpaRepository.findByAccountIdAndFunctionalityAndActive(
                account1.getId(), "TRANSFER", LocalDateTime.now());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValue()).isEqualTo("user-1");

        List<BlacklistEntity> result2 = blacklistJpaRepository.findByAccountIdAndFunctionalityAndActive(
                account2.getId(), "PAYMENT", LocalDateTime.now());
        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getValue()).isEqualTo("user-3");
    }

    @Test
    @DisplayName("findByAccountIdAndFunctionalityAndActive should return empty for expired or non-matching functionality")
    void findByAccountIdAndFunctionalityAndActive_shouldReturnEmptyForExpiredOrNonMatching() {
        List<BlacklistEntity> result = blacklistJpaRepository.findByAccountIdAndFunctionalityAndActive(
                account1.getId(), "PAYMENT", LocalDateTime.now());
        assertThat(result).isEmpty();

        List<BlacklistEntity> result2 = blacklistJpaRepository.findByAccountIdAndFunctionalityAndActive(
                account1.getId(), "TRANSFER", LocalDateTime.now().plusDays(2));
        assertThat(result2).isEmpty();
    }

    @Test
    @DisplayName("should save and retrieve blacklist entity")
    void shouldSaveAndRetrieveBlacklistEntity() {
        BlacklistEntity entity = new BlacklistEntity();
        entity.setFunctionality("LOGIN");
        entity.setEntityType("USER");
        entity.setValue("user-4");
        entity.setStartDate(LocalDateTime.now());
        entity.setEndDate(null);
        entity.setAccountId(account1.getId());
        entity.setAccount(account1);

        BlacklistEntity saved = blacklistJpaRepository.save(entity);
        assertThat(saved.getId()).isNotNull();

        Optional<BlacklistEntity> found = blacklistJpaRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFunctionality()).isEqualTo("LOGIN");
    }

    @Test
    @DisplayName("should update blacklist entity")
    void shouldUpdateBlacklistEntity() {
        activeBlacklist1.setFunctionality("UPDATED");
        BlacklistEntity updated = blacklistJpaRepository.save(activeBlacklist1);
        assertThat(updated.getFunctionality()).isEqualTo("UPDATED");
    }

    @Test
    @DisplayName("should delete blacklist entity by id")
    void shouldDeleteBlacklistEntityById() {
        Long id = activeBlacklist2.getId();
        blacklistJpaRepository.deleteById(id);
        Optional<BlacklistEntity> found = blacklistJpaRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll should return all blacklist entities")
    void findAll_shouldReturnAllEntities() {
        List<BlacklistEntity> all = blacklistJpaRepository.findAll();
        assertThat(all).hasSize(3);
        assertThat(all).extracting(BlacklistEntity::getValue)
                .containsExactlyInAnyOrder("user-1", "user-2", "user-3");
    }
}

