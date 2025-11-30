package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.AccountSettingsEntity;
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
@DisplayName("AccountSettingsJpaRepository Tests")
class AccountSettingsJpaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountSettingsJpaRepository accountSettingsJpaRepository;

    private AccountSettingsEntity emailNotificationSetting;
    private AccountSettingsEntity smsNotificationSetting;
    private AccountSettingsEntity pushNotificationSetting;
    private AccountSettingsEntity anotherAccountSetting;

    @BeforeEach
    void setUp() {
        emailNotificationSetting = AccountSettingsEntity.builder()
                .key("notification.email")
                .value("enabled")
                .capabilities("EMAIL,SMS")
                .consumers("mobile,web")
                .accountId(1L)
                .build();

        smsNotificationSetting = AccountSettingsEntity.builder()
                .key("notification.sms")
                .value("disabled")
                .capabilities("SMS")
                .consumers("mobile")
                .accountId(1L)
                .build();

        pushNotificationSetting = AccountSettingsEntity.builder()
                .key("notification.push")
                .value("enabled")
                .capabilities("PUSH")
                .consumers("web")
                .accountId(1L)
                .build();

        anotherAccountSetting = AccountSettingsEntity.builder()
                .key("notification.email")
                .value("disabled")
                .capabilities("EMAIL")
                .consumers("mobile")
                .accountId(2L)
                .build();

        entityManager.persistAndFlush(emailNotificationSetting);
        entityManager.persistAndFlush(smsNotificationSetting);
        entityManager.persistAndFlush(pushNotificationSetting);
        entityManager.persistAndFlush(anotherAccountSetting);
    }

    @Test
    @DisplayName("should find settings by account ID and capability containing")
    void shouldFindByAccountIdAndCapabilitiesContaining() {
        List<AccountSettingsEntity> emailCapableSettings = accountSettingsJpaRepository
                .findByAccountIdAndCapabilitiesContaining(1L, "EMAIL");

        assertThat(emailCapableSettings).hasSize(1);
        assertThat(emailCapableSettings.get(0).getKey()).isEqualTo("notification.email");
        assertThat(emailCapableSettings.get(0).getCapabilities()).contains("EMAIL");
    }

    @Test
    @DisplayName("should find settings by account ID and consumer containing")
    void shouldFindByAccountIdAndConsumersContaining() {
        List<AccountSettingsEntity> mobileConsumerSettings = accountSettingsJpaRepository
                .findByAccountIdAndConsumersContaining(1L, "mobile");

        assertThat(mobileConsumerSettings).hasSize(2);
        assertThat(mobileConsumerSettings)
                .extracting(AccountSettingsEntity::getKey)
                .containsExactlyInAnyOrder("notification.email", "notification.sms");
    }

    @Test
    @DisplayName("should find all settings by account ID")
    void shouldFindByAccountId() {
        List<AccountSettingsEntity> account1Settings = accountSettingsJpaRepository.findByAccountId(1L);

        assertThat(account1Settings).hasSize(3);
        assertThat(account1Settings)
                .extracting(AccountSettingsEntity::getAccountId)
                .containsOnly(1L);
    }

    @Test
    @DisplayName("should find setting by ID and account ID")
    void shouldFindByIdAndAccountId() {
        Optional<AccountSettingsEntity> foundSetting = accountSettingsJpaRepository
                .findByIdAndAccountId(emailNotificationSetting.getId(), 1L);

        assertThat(foundSetting).isPresent();
        assertThat(foundSetting.get().getKey()).isEqualTo("notification.email");
        assertThat(foundSetting.get().getValue()).isEqualTo("enabled");
    }

    @Test
    @DisplayName("should return empty when finding by non-matching account ID")
    void shouldReturnEmptyWhenFindingByNonMatchingAccountId() {
        Optional<AccountSettingsEntity> notFoundSetting = accountSettingsJpaRepository
                .findByIdAndAccountId(emailNotificationSetting.getId(), 999L);

        assertThat(notFoundSetting).isEmpty();
    }

    @Test
    @DisplayName("should find settings by account ID and key")
    void shouldFindByAccountIdAndKey() {
        List<AccountSettingsEntity> emailSettings = accountSettingsJpaRepository
                .findByAccountIdAndKey(1L, "notification.email");

        assertThat(emailSettings).hasSize(1);
        assertThat(emailSettings.get(0).getKey()).isEqualTo("notification.email");
        assertThat(emailSettings.get(0).getValue()).isEqualTo("enabled");
    }

    @Test
    @DisplayName("should return empty list when key not found for account")
    void shouldReturnEmptyListWhenKeyNotFoundForAccount() {
        List<AccountSettingsEntity> nonExistentSettings = accountSettingsJpaRepository
                .findByAccountIdAndKey(1L, "non.existent.key");

        assertThat(nonExistentSettings).isEmpty();
    }

    @Test
    @DisplayName("should update setting value successfully")
    void shouldUpdateSettingValue() {
        int updatedCount = accountSettingsJpaRepository
                .updateSettingValue("notification.email", "disabled", 1L);

        assertThat(updatedCount).isEqualTo(1);

        entityManager.clear();
        AccountSettingsEntity updatedEntity = entityManager.find(
                AccountSettingsEntity.class,
                emailNotificationSetting.getId()
        );
        assertThat(updatedEntity.getValue()).isEqualTo("disabled");
    }

    @Test
    @DisplayName("should return zero when updating non-existing setting")
    void shouldReturnZeroWhenUpdatingNonExistingSetting() {
        int updatedCount = accountSettingsJpaRepository
                .updateSettingValue("non.existing.key", "value", 1L);

        assertThat(updatedCount).isZero();
    }

    @Test
    @DisplayName("should save and retrieve new entity")
    void shouldSaveAndRetrieveNewEntity() {
        AccountSettingsEntity newSetting = AccountSettingsEntity.builder()
                .key("theme.mode")
                .value("dark")
                .capabilities("UI")
                .consumers("web,mobile")
                .accountId(3L)
                .build();

        AccountSettingsEntity savedSetting = accountSettingsJpaRepository.save(newSetting);

        assertThat(savedSetting.getId()).isNotNull();
        assertThat(savedSetting.getKey()).isEqualTo("theme.mode");

        Optional<AccountSettingsEntity> retrievedSetting = accountSettingsJpaRepository
                .findById(savedSetting.getId());
        assertThat(retrievedSetting).isPresent();
        assertThat(retrievedSetting.get().getValue()).isEqualTo("dark");
    }

    @Test
    @DisplayName("should delete entity by ID")
    void shouldDeleteEntityById() {
        Long settingId = emailNotificationSetting.getId();

        accountSettingsJpaRepository.deleteById(settingId);

        Optional<AccountSettingsEntity> deletedSetting = accountSettingsJpaRepository.findById(settingId);
        assertThat(deletedSetting).isEmpty();
    }

    @Test
    @DisplayName("should handle empty results gracefully")
    void shouldHandleEmptyResultsGracefully() {
        List<AccountSettingsEntity> nonExistentAccountSettings = accountSettingsJpaRepository
                .findByAccountId(999L);

        assertThat(nonExistentAccountSettings).isEmpty();
    }

    @Test
    @DisplayName("should find multiple settings with same capability")
    void shouldFindMultipleSettingsWithSameCapability() {
        // Create additional setting with SMS capability
        AccountSettingsEntity additionalSmsSetting = AccountSettingsEntity.builder()
                .key("sms.frequency")
                .value("daily")
                .capabilities("SMS")
                .consumers("mobile")
                .accountId(1L)
                .build();
        entityManager.persistAndFlush(additionalSmsSetting);

        List<AccountSettingsEntity> smsCapableSettings = accountSettingsJpaRepository
                .findByAccountIdAndCapabilitiesContaining(1L, "SMS");

        assertThat(smsCapableSettings).hasSize(3); // original SMS + EMAIL,SMS + new SMS setting
    }
}

