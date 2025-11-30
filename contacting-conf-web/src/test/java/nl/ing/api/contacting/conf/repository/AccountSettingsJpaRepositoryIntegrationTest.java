package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.AccountSettingsEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AccountSettingsJpaRepository Integration Tests")
class AccountSettingsJpaRepositoryIntegrationTest {

    @Autowired
    private AccountSettingsJpaRepository accountSettingsJpaRepository;

    @Test
    @DisplayName("should handle complex capability searches across multiple accounts")
    void shouldHandleComplexCapabilitySearchesAcrossMultipleAccounts() {
        // Create test data for multiple accounts
        var account1EmailSetting = AccountSettingsEntity.builder()
                .key("email.notifications")
                .value("enabled")
                .capabilities("EMAIL,PUSH")
                .consumers("web,mobile")
                .accountId(100L)
                .build();

        var account1SmsSetting = AccountSettingsEntity.builder()
                .key("sms.notifications")
                .value("disabled")
                .capabilities("SMS")
                .consumers("mobile")
                .accountId(100L)
                .build();

        var account2EmailSetting = AccountSettingsEntity.builder()
                .key("email.notifications")
                .value("enabled")
                .capabilities("EMAIL")
                .consumers("web")
                .accountId(200L)
                .build();

        accountSettingsJpaRepository.saveAll(List.of(
                account1EmailSetting,
                account1SmsSetting,
                account2EmailSetting
        ));

        // Test capability search for account 1
        List<AccountSettingsEntity> account1EmailCapable = accountSettingsJpaRepository
                .findByAccountIdAndCapabilitiesContaining(100L, "EMAIL");

        assertThat(account1EmailCapable).hasSize(1);
        assertThat(account1EmailCapable.get(0).getKey()).isEqualTo("email.notifications");

        // Test capability search for account 2
        List<AccountSettingsEntity> account2EmailCapable = accountSettingsJpaRepository
                .findByAccountIdAndCapabilitiesContaining(200L, "EMAIL");

        assertThat(account2EmailCapable).hasSize(1);
        assertThat(account2EmailCapable.get(0).getAccountId()).isEqualTo(200L);

        // Test consumer search
        List<AccountSettingsEntity> mobileConsumers = accountSettingsJpaRepository
                .findByAccountIdAndConsumersContaining(100L, "mobile");

        assertThat(mobileConsumers).hasSize(2);
    }

    @Test
    @DisplayName("should maintain referential integrity")
    void shouldMaintainReferentialIntegrity() {
        var setting = AccountSettingsEntity.builder()
                .key("integrity.test")
                .value("test")
                .capabilities("TEST")
                .consumers("test")
                .accountId(400L)
                .build();

        AccountSettingsEntity savedSetting = accountSettingsJpaRepository.save(setting);
        assertThat(savedSetting.getId()).isNotNull();

        // Find by different criteria should return the same entity
        var foundById = accountSettingsJpaRepository.findById(savedSetting.getId());
        var foundByAccountAndKey = accountSettingsJpaRepository
                .findByAccountIdAndKey(400L, "integrity.test");
        var foundByAccount = accountSettingsJpaRepository.findByAccountId(400L);

        assertThat(foundById).isPresent();
        assertThat(foundByAccountAndKey).hasSize(1);
        assertThat(foundByAccount).hasSize(1);
        assertThat(foundById.get().getId()).isEqualTo(foundByAccountAndKey.get(0).getId());
        assertThat(foundById.get().getId()).isEqualTo(foundByAccount.get(0).getId());
    }
}

