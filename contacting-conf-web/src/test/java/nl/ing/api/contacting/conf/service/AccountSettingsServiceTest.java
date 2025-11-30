package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.AllAccountSettings;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingConsumers;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingVO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.conf.helper.AccountsTestData;
import nl.ing.api.contacting.conf.repository.AccountSettingsAuditRepository;
import nl.ing.api.contacting.conf.repository.PlatformAccountSettingsCacheRepository;
import nl.ing.api.contacting.conf.repository.PlatformAccountSettingsJpaRepository;
import nl.ing.api.contacting.shared.client.ContactingAPIClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class AccountSettingsServiceTest {

    private AccountSettingsAuditRepository accountSettingsRepository;
    private PlatformAccountSettingsCacheRepository platformAccountSettingsCacheRepository;
    private SettingsMetadataServiceJava settingsMetadataServiceJava;
    private ContactingAPIClient contactingAPIClient;
    private AccountSettingsServiceJava service;
    private ContactingContext context;

    @BeforeEach
    void setUp() {
        accountSettingsRepository = mock(AccountSettingsAuditRepository.class);
        platformAccountSettingsCacheRepository = mock(PlatformAccountSettingsCacheRepository.class);
        contactingAPIClient = mock(ContactingAPIClient.class);
        settingsMetadataServiceJava = mock(SettingsMetadataServiceJava.class);
        service = new AccountSettingsServiceJava(contactingAPIClient, accountSettingsRepository, platformAccountSettingsCacheRepository, settingsMetadataServiceJava);
        context = new ContactingContext(101L, null);
    }

    @Nested
    @DisplayName("getAllSettings")
    class GetAllSettings {

        @Test
        @DisplayName("should return AllAccountSettingsVO with data from both repositories")
        void shouldReturnAllSettings_whenBothRepositoriesReturnData() {

            when(accountSettingsRepository.findAllByAccount(context))
                    .thenReturn(List.of(AccountsTestData.getAccountSettingEntity()));
            when(platformAccountSettingsCacheRepository.findByAccountId(context))
                    .thenReturn(List.of(AccountsTestData.getPlatformSettingEntity()));

            AllAccountSettings result = service.getAllSettings(context);

            assertEquals(1, result.accountSettings().size());
            assertEquals("TIMEZONE", result.accountSettings().get(0).key());
            assertEquals("Asia/Delhi", result.accountSettings().get(0).value());
            assertEquals(1, result.platformAccountSettings().size());
            assertEquals("lease_line_flag", result.platformAccountSettings().get(0).key());
            assertEquals("true", result.platformAccountSettings().get(0).value());
            verify(accountSettingsRepository, times(1)).findAllByAccount(context);
        }

        @Test
        void shouldReturnEmptyAccountSettings_whenAccountRepoEmpty() throws Exception {

            when(accountSettingsRepository.findAllByAccount(context))
                    .thenReturn(List.of());
            when(platformAccountSettingsCacheRepository.findByAccountId(context))
                    .thenReturn(List.of(AccountsTestData.getPlatformSettingEntity()));

            AllAccountSettings result = service.getAllSettings(context);

            assertTrue(result.accountSettings().isEmpty());
            assertEquals(1, result.platformAccountSettings().size());
        }


        @Test
        void shouldReturnEmptyPlatformSettings_whenPlatformRepoEmpty() throws Exception {

            when(accountSettingsRepository.findAllByAccount(context))
                    .thenReturn(List.of(AccountsTestData.getAccountSettingEntity()));
            when(platformAccountSettingsCacheRepository.findByAccountId(context))
                    .thenReturn(List.of());

            AllAccountSettings result = service.getAllSettings(context);

            assertEquals(1, result.accountSettings().size());
            assertTrue(result.platformAccountSettings().isEmpty());
        }

        @Test
        void shouldReturnEmptyLists_whenBothRepositoriesEmpty() throws Exception {
            when(accountSettingsRepository.findAllByAccount(context))
                    .thenReturn(List.of());
            when(platformAccountSettingsCacheRepository.findByAccountId(context))
                    .thenReturn(List.of());

            AllAccountSettings result = service.getAllSettings(context);

            assertTrue(result.accountSettings().isEmpty());
            assertTrue(result.platformAccountSettings().isEmpty());
        }

        @Test
        void shouldFail_whenAccountRepositoryThrowsException() {
            when(accountSettingsRepository.findAllByAccount(context))
                    .thenThrow(new RuntimeException("DB error"));
            when(platformAccountSettingsCacheRepository.findByAccountId(context))
                    .thenReturn(List.of());


            RuntimeException ex = assertThrows(RuntimeException.class, ()->service.getAllSettings(context));
            assertTrue(ex instanceof RuntimeException);
            assertEquals("DB error", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("getAllSettings (by capabilities)")
    class GetAllSettingsWithCapabilities {

        private Set<SettingCapability> capabilities;

        @BeforeEach
        void setup() {
            capabilities = Set.of(SettingCapability.CHAT, SettingCapability.VIDEO);
        }

        @Test
        @DisplayName("should return filtered settings when repositories return matching data")
        void shouldReturnFilteredSettings() throws Exception {

            when(accountSettingsRepository.findByCapabilities(
                    capabilities,
                    context.accountId()))
                    .thenReturn(List.of(AccountsTestData.getAccountSettingEntity()));


            when(platformAccountSettingsCacheRepository.findByAccountId(context))
                    .thenReturn(List.of(AccountsTestData.getPlatformSettingEntity()));

            AllAccountSettings result = service.getAllSettings(context, capabilities);

            // then
            assertEquals(1, result.accountSettings().size());
            assertEquals(1, result.platformAccountSettings().size());
            assertEquals("TIMEZONE", result.accountSettings().get(0).key());

            verify(accountSettingsRepository, times(1))
                    .findByCapabilities(anySet(), eq(context.accountId()));
        }

        @Test
        @DisplayName("should return empty account settings when repository returns none")
        void shouldReturnEmptyAccountSettings() throws Exception {
            when(accountSettingsRepository.findByCapabilities(anySet(), eq(context.accountId())))
                    .thenReturn(List.of());

            when(platformAccountSettingsCacheRepository.findByAccountId(context))
                    .thenReturn(List.of(
                            AccountsTestData.getPlatformSettingEntity()
                    ));

            AllAccountSettings result = service.getAllSettings(context, capabilities);

            assertTrue(result.accountSettings().isEmpty());
            assertEquals(1, result.platformAccountSettings().size());
        }

        @Test
        @DisplayName("should return empty lists when both repositories return empty")
        void shouldReturnEmptyLists() throws Exception {
            when(accountSettingsRepository.findByCapabilities(anySet(), anyLong()))
                    .thenReturn(List.of());

            when(platformAccountSettingsCacheRepository.findByAccountId(context))
                    .thenReturn(List.of());

            AllAccountSettings result = service.getAllSettings(context, capabilities);

            assertTrue(result.accountSettings().isEmpty());
            assertTrue(result.platformAccountSettings().isEmpty());
        }

        @Test
        @DisplayName("should fail when account repository throws exception")
        void shouldFailWhenAccountRepositoryThrows() {
            when(accountSettingsRepository.findByCapabilities(anySet(), anyLong()))
                    .thenThrow(new RuntimeException("DB failure"));

            when(platformAccountSettingsCacheRepository.findByAccountId(context))
                    .thenReturn(List.of());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getAllSettings(context, capabilities));
            assertTrue(ex instanceof RuntimeException);
            assertEquals("DB failure", ex.getMessage());
        }

        @Test
        @DisplayName("should handle null or empty capabilities gracefully")
        void shouldHandleEmptyCapabilities() throws Exception {
            when(accountSettingsRepository.findByCapabilities(anySet(), anyLong()))
                    .thenReturn(List.of());

            when(platformAccountSettingsCacheRepository.findByAccountId(context))
                    .thenReturn(List.of());

            AllAccountSettings result = service.getAllSettings(context, Set.of());

            assertTrue(result.accountSettings().isEmpty());
            assertTrue(result.platformAccountSettings().isEmpty());
        }
    }


    @Nested
    @DisplayName("getAccountSettingsForCustomers")
    class GetAccountSettingsForCustomers {

        @Test
        @DisplayName("should return account settings for customers when repository returns data")
        void shouldReturnCustomerSettings() throws Exception {

            when(accountSettingsRepository.getCustomerSettings(
                    eq(context.accountId()),
                    eq(AccountSettingConsumers.CUSTOMER.value())
            )).thenReturn(List.of(AccountsTestData.getAccountSettingEntity()));

            // when
            List<AccountSettingVO> result = service.getAccountSettingsForCustomers(context);

            // then
            assertEquals(1, result.size());
            assertEquals("TIMEZONE", result.get(0).key());
            assertEquals("Asia/Delhi", result.get(0).value());
            verify(accountSettingsRepository).getCustomerSettings(
                    eq(context.accountId()),
                    eq(AccountSettingConsumers.CUSTOMER.value())
            );
        }

        @Test
        @DisplayName("should return empty list when repository returns none")
        void shouldReturnEmptyList() throws Exception {
            when(accountSettingsRepository.getCustomerSettings(
                    eq(context.accountId()),
                    eq(AccountSettingConsumers.CUSTOMER.value())
            )).thenReturn(List.of());

            List<AccountSettingVO> result = service.getAccountSettingsForCustomers(context);

            assertTrue(result.isEmpty());
            verify(accountSettingsRepository).getCustomerSettings(
                    eq(context.accountId()),
                    eq(AccountSettingConsumers.CUSTOMER.value())
            );
        }

        @Test
        @DisplayName("should fail when repository throws exception")
        void shouldFailWhenRepositoryFails() {
            when(accountSettingsRepository.getCustomerSettings(
                    eq(context.accountId()),
                    eq(AccountSettingConsumers.CUSTOMER.value())
            )).thenThrow(new RuntimeException("DB failure"));

            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getAccountSettingsForCustomers(context));
            assertTrue(ex instanceof RuntimeException);
            assertEquals("DB failure", ex.getMessage());
        }
    }
}


