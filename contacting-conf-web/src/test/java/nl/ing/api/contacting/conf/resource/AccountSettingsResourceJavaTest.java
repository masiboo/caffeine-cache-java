package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import jakarta.ws.rs.core.Response;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingVO;
import nl.ing.api.contacting.conf.domain.model.admintool.AccountSettingDTO;
import nl.ing.api.contacting.conf.domain.model.admintool.CustomerAccountSettingsDTO;
import nl.ing.api.contacting.conf.helper.AccountsTestData;
import nl.ing.api.contacting.conf.service.AccountSettingsServiceJava;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountSettingsResourceJavaTest {

    @Mock
    private AccountSettingsServiceJava mockService;

    private AccountSettingsResourceJava resource;

    @BeforeEach
    void setUp() {
        resource = new TestableCustomerAccountSettingsResourceJava(mockService);
    }

    @Nested
    @DisplayName("getCustomers")
    class GetCustomers {

        @Test
        @DisplayName("should return customer account settings successfully")
        void shouldReturnCustomerAccountSettings() throws Exception {

            List<AccountSettingVO> mockSettings = List.of(AccountsTestData.getAccountSettingVO());

            when(mockService.getAccountSettingsForCustomers(any(ContactingContext.class)))
                    .thenReturn(mockSettings);

            // when
            CompletableFuture<Response> responseFuture = resource.getCustomers();
            Response response = responseFuture.get();
            assertEquals(200, response.getStatus());

            CustomerAccountSettingsDTO dto = (CustomerAccountSettingsDTO) response.getEntity();
            assertEquals(1, dto.settings().size());
            assertEquals("TIMEZONE", dto.settings().get(0).key());
        }

        @Test
        @DisplayName("should handle empty customer settings list gracefully")
        void shouldReturnEmptyListWhenNoSettings() throws Exception {

            when(mockService.getAccountSettingsForCustomers(any(ContactingContext.class)))
                    .thenReturn(List.of());

            CompletableFuture<Response> responseFuture = resource.getCustomers();
            Response response = responseFuture.get();

            assertEquals(200, response.getStatus());
            CustomerAccountSettingsDTO dto = (CustomerAccountSettingsDTO) response.getEntity();
            assertTrue(dto.settings().isEmpty());
        }
    }

    @Nested
    @DisplayName("createAccountSetting")
    class CreateAccountSetting {

        @Test
        @DisplayName("should create a new account setting successfully")
        void shouldCreateAccountSetting() throws Exception {
            AccountSettingDTO request = AccountsTestData.getAdminToolElementsAccountSettingDTO();
            when(mockService.upsertAccountSetting(any(AccountSettingVO.class), any(ContactingContext.class)))
                    .thenReturn(request);

            CompletableFuture<Response> responseFuture = resource.create(request);
            Response response = responseFuture.get();

            assertEquals(201, response.getStatus());
            AccountSettingDTO result = (AccountSettingDTO) response.getEntity();
            assertEquals(request.key(), result.key());
        }
    }

    @Test
    @DisplayName("should return null entity when service returns null")
    void shouldReturnBadRequestWhenServiceReturnsNull() throws Exception {
        AccountSettingDTO request = AccountsTestData.getAdminToolElementsAccountSettingDTO();

        // Service returns null instead of a created object
        when(mockService.upsertAccountSetting(any(AccountSettingVO.class), any(ContactingContext.class)))
                .thenReturn(null);

        CompletableFuture<Response> responseFuture = resource.create(request);
        Response response = responseFuture.get();

        Assertions.assertNull(response.getEntity());
    }

    @Nested
    @DisplayName("updateAccountSetting")
    class UpdateAccountSetting {

        @Test
        @DisplayName("should update existing account setting successfully")
        void shouldUpdateAccountSetting() throws Exception {
            long id = 42L;
            AccountSettingDTO updated = AccountsTestData.getAdminToolElementsAccountSettingDTO();

            when(mockService.upsertAccountSetting(any(AccountSettingVO.class), any(ContactingContext.class)))
                    .thenReturn(updated);

            CompletableFuture<Response> responseFuture = resource.update(id, updated);
            Response response = responseFuture.get();

            assertEquals(200, response.getStatus());
            AccountSettingDTO result = (AccountSettingDTO) response.getEntity();
            assertEquals(updated.key(), result.key());
        }
    }

    @Nested
    @DisplayName("deleteAccountSetting")
    class DeleteAccountSetting {

        @Test
        @DisplayName("should delete account setting successfully")
        void shouldDeleteAccountSetting() throws Exception {
            long id = 1L;
            when(mockService.findById(org.mockito.ArgumentMatchers.eq(id), any(ContactingContext.class))).thenReturn(AccountsTestData.getAccountSettingsEntity());
            CompletableFuture<Void> future = resource.delete(id);
            future.get();
        }
    }

    static class TestableCustomerAccountSettingsResourceJava extends AccountSettingsResourceJava {
        TestableCustomerAccountSettingsResourceJava(AccountSettingsServiceJava service) {
            super(service);
        }

        @Override
        protected ContactingContext getContactingContext() {
            return new ContactingContext(101L, null);
        }
    }
}