package nl.ing.api.contacting.conf.repository;

import com.ing.api.contacting.dto.java.audit.AuditedEntity;
import com.ing.api.contacting.dto.java.context.ContactingContext;
import nl.ing.api.contacting.conf.domain.entity.AccountSettingsEntity;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.conf.domain.model.webhook.UpdateSetting;
import nl.ing.api.contacting.java.repository.jpa.audit.AuditEntityActions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountSettingsAuditRepository Tests")
class AccountSettingsAuditRepositoryTest {

    @Mock
    private AccountSettingsJpaRepository jpaRepository;

    @Mock
    private AuditEntityActions<AccountSettingsEntity, Long> auditActions;

    @Mock
    private ContactingContext contactingContext;

    private AccountSettingsAuditRepository accountSettingsAuditRepository;

    private AccountSettingsEntity testSetting;
    private List<AccountSettingsEntity> testSettings;

    @BeforeEach
    void setUp() {
        accountSettingsAuditRepository = new AccountSettingsAuditRepository(jpaRepository, auditActions);

        testSetting = AccountSettingsEntity.builder()
                .id(1L)
                .key("notification.email")
                .value("enabled")
                .capabilities("EMAIL,SMS")
                .consumers("mobile,web")
                .accountId(123L)
                .build();

        testSettings = List.of(testSetting);
    }

    @Test
    @DisplayName("should find settings by capabilities")
    void shouldFindByCapabilities() {
        Set<SettingCapability> capabilities = Set.of(
                SettingCapability.fromValue("chat"),
                SettingCapability.fromValue("video")
        );

        when(jpaRepository.findByAccountIdAndCapabilitiesContaining(123L, "chat"))
                .thenReturn(testSettings);
        when(jpaRepository.findByAccountIdAndCapabilitiesContaining(123L, "video"))
                .thenReturn(testSettings);

        List<AccountSettingsEntity> foundSettings = accountSettingsAuditRepository
                .findByCapabilities(capabilities, 123L);

        assertThat(foundSettings).hasSize(1);
        assertThat(foundSettings.get(0)).isEqualTo(testSetting);
        verify(jpaRepository).findByAccountIdAndCapabilitiesContaining(123L, "chat");
        verify(jpaRepository).findByAccountIdAndCapabilitiesContaining(123L, "video");
    }

    @Test
    @DisplayName("should return empty list when no capabilities provided")
    void shouldReturnEmptyListWhenNoCapabilitiesProvided() {
        List<AccountSettingsEntity> foundSettings = accountSettingsAuditRepository
                .findByCapabilities(Set.of(), 123L);

        assertThat(foundSettings).isEmpty();
        verifyNoInteractions(jpaRepository);
    }

    @Test
    @DisplayName("should find all settings by account")
    void shouldFindAllByAccount() {
        when(jpaRepository.findByAccountId(123L)).thenReturn(testSettings);
        when(contactingContext.accountId()).thenReturn(123L);
        List<AccountSettingsEntity> allSettings = accountSettingsAuditRepository
                .findAllByAccount(contactingContext);

        assertThat(allSettings).hasSize(1);
        assertThat(allSettings.get(0)).isEqualTo(testSetting);
        verify(jpaRepository).findByAccountId(123L);
    }

    @Test
    @DisplayName("should find setting by ID")
    void shouldFindById() {
        when(jpaRepository.findByIdAndAccountId(1L, 123L)).thenReturn(Optional.of(testSetting));
        when(contactingContext.accountId()).thenReturn(123L);
        Optional<AccountSettingsEntity> foundSetting = accountSettingsAuditRepository
                .findById(1L, contactingContext);

        assertThat(foundSetting).isPresent();
        assertThat(foundSetting.get()).isEqualTo(testSetting);
        verify(jpaRepository).findByIdAndAccountId(1L, 123L);
    }

    @Test
    @DisplayName("should return empty when setting not found by ID")
    void shouldReturnEmptyWhenSettingNotFoundById() {
        when(jpaRepository.findByIdAndAccountId(999L, 123L)).thenReturn(Optional.empty());
        when(contactingContext.accountId()).thenReturn(123L);
        Optional<AccountSettingsEntity> foundSetting = accountSettingsAuditRepository
                .findById(999L, contactingContext);

        assertThat(foundSetting).isEmpty();
        verify(jpaRepository).findByIdAndAccountId(999L, 123L);
    }

    @Test
    @DisplayName("should get customer settings by consumer")
    void shouldGetCustomerSettingsByConsumer() {
        when(jpaRepository.findByAccountIdAndConsumersContaining(123L, "mobile"))
                .thenReturn(testSettings);

        List<AccountSettingsEntity> customerSettings = accountSettingsAuditRepository
                .getCustomerSettings(123L, "mobile");

        assertThat(customerSettings).hasSize(1);
        assertThat(customerSettings.get(0)).isEqualTo(testSetting);
        verify(jpaRepository).findByAccountIdAndConsumersContaining(123L, "mobile");
    }

    @Test
    @DisplayName("should save and audit entity")
    void shouldSaveAndAudit() {
        when(jpaRepository.save(testSetting)).thenReturn(testSetting);

        AccountSettingsEntity savedSetting = accountSettingsAuditRepository
                .saveAndAudit(testSetting, contactingContext);

        assertThat(savedSetting).isEqualTo(testSetting);
        verify(jpaRepository).save(testSetting);
        verify(auditActions).auditInsert(testSetting, contactingContext);
    }

    @Test
    @DisplayName("should update and audit entity")
    void shouldUpdateAndAudit() {
        when(jpaRepository.save(testSetting)).thenReturn(testSetting);

        AccountSettingsEntity updatedSetting = accountSettingsAuditRepository
                .updateAndAudit(testSetting, contactingContext);

        assertThat(updatedSetting).isEqualTo(testSetting);
        verify(jpaRepository).save(testSetting);
        verify(auditActions).auditUpdate(testSetting, contactingContext);
    }

    @Test
    @DisplayName("should delete and audit entity")
    void shouldDeleteAndAudit() {
        accountSettingsAuditRepository.deleteAndAudit(1L, contactingContext);

        verify(jpaRepository).deleteById(1L);
        verify(auditActions).auditDelete(1L, 1, contactingContext);
    }

    @Test
    @DisplayName("should get audit history")
    void shouldGetAuditHistory() {
        @SuppressWarnings("unchecked")
        AuditedEntity<AccountSettingsEntity, Long> mockAuditedEntity = mock(AuditedEntity.class);
        @SuppressWarnings("unchecked")
        List<AuditedEntity<AccountSettingsEntity, Long>> mockAuditHistory = List.of(mockAuditedEntity);
        when(auditActions.getAuditedVersions(1L, 10)).thenReturn(mockAuditHistory);

        List<AuditedEntity<AccountSettingsEntity, Long>> auditHistory =
                accountSettingsAuditRepository.getAuditHistory(1L, 10);

        assertThat(auditHistory).hasSize(1);
        verify(auditActions).getAuditedVersions(1L, 10);
    }

    @Test
    @DisplayName("should update all settings")
    void shouldUpdateAllSettings() {
        List<UpdateSetting> updateSettings = List.of(
                new UpdateSetting("notification.email", "disabled", 123L),
                new UpdateSetting("notification.sms", "enabled", 123L)
        );

        when(jpaRepository.updateSettingValue("notification.email", "disabled", 123L)).thenReturn(1);
        when(jpaRepository.updateSettingValue("notification.sms", "enabled", 123L)).thenReturn(1);

        List<Integer> updateResults = accountSettingsAuditRepository.updateAll(updateSettings);

        assertThat(updateResults).containsExactly(1, 1);
        verify(jpaRepository).updateSettingValue("notification.email", "disabled", 123L);
        verify(jpaRepository).updateSettingValue("notification.sms", "enabled", 123L);
    }

    @Test
    @DisplayName("should handle failed updates in batch")
    void shouldHandleFailedUpdatesInBatch() {
        List<UpdateSetting> updateSettings = List.of(
                new UpdateSetting("existing.key", "new.value", 123L),
                new UpdateSetting("non.existing.key", "value", 123L)
        );

        when(jpaRepository.updateSettingValue("existing.key", "new.value", 123L)).thenReturn(1);
        when(jpaRepository.updateSettingValue("non.existing.key", "value", 123L)).thenReturn(0);

        List<Integer> updateResults = accountSettingsAuditRepository.updateAll(updateSettings);

        assertThat(updateResults).containsExactly(1, 0);
    }

    @Test
    @DisplayName("should handle empty update list")
    void shouldHandleEmptyUpdateList() {
        List<Integer> updateResults = accountSettingsAuditRepository.updateAll(List.of());

        assertThat(updateResults).isEmpty();
        verifyNoInteractions(jpaRepository);
    }

    @Test
    @DisplayName("should remove duplicates when finding by multiple capabilities")
    void shouldRemoveDuplicatesWhenFindingByMultipleCapabilities() {
        // Both capabilities return the same setting
        Set<SettingCapability> capabilities = Set.of(
                SettingCapability.fromValue("chat"),
                SettingCapability.fromValue("video")
        );

        when(jpaRepository.findByAccountIdAndCapabilitiesContaining(123L, "chat"))
                .thenReturn(testSettings);
        when(jpaRepository.findByAccountIdAndCapabilitiesContaining(123L, "video"))
                .thenReturn(testSettings); // Same setting returned for both capabilities

        List<AccountSettingsEntity> foundSettings = accountSettingsAuditRepository
                .findByCapabilities(capabilities, 123L);

        assertThat(foundSettings).hasSize(1); // Should be deduplicated
        assertThat(foundSettings.get(0)).isEqualTo(testSetting);
    }
}

