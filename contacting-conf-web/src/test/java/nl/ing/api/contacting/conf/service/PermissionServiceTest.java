package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.AuditContext;
import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.resource.account.AccountDto;
import nl.ing.api.contacting.conf.configuration.AuditLoggerService;
import nl.ing.api.contacting.conf.domain.entity.cassandra.BusinessFunctionOnTeamEntity;
import nl.ing.api.contacting.conf.domain.entity.cassandra.ContactingConfigEntity;
import nl.ing.api.contacting.conf.domain.model.permission.*;
import nl.ing.api.contacting.conf.helper.PermissionTestData;
import nl.ing.api.contacting.conf.repository.EmployeesByAccountRepository;
import nl.ing.api.contacting.conf.repository.PermissionCacheRepository;
import nl.ing.api.contacting.trust.rest.context.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static nl.ing.api.contacting.conf.domain.model.permission.OrganisationalRestrictionLevel.TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private ContactingConfigService contactingConfigService;
    @Mock
    private EmployeesByAccountRepository employeesByAccountRepository;
    @Mock
    private PermissionCacheRepository permissionCacheRepository;

    private PermissionService permissionService;
    private ContactingContext contactingContext;
    private AuditContext auditContext;
    private AuditLoggerService auditLoggerService;
    private static final String ACCOUNT_FRIENDLY_NAME = "test-account";

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService(
            contactingConfigService,
            employeesByAccountRepository,
            permissionCacheRepository,
                auditLoggerService
        );
        contactingContext = mock(ContactingContext.class);
        auditContext = mock(AuditContext.class);
        auditLoggerService   = mock(AuditLoggerService.class);
    }

    @Nested
    @DisplayName("getEditableBusinessFunctions tests")
    class GetEditableBusinessFunctionsTests {

        @Test
        @DisplayName("Should return filtered business functions when cache returns data")
        void shouldReturnFilteredBusinessFunctionsWhenCacheReturnsData() {
            // Given
            List<BusinessFunctionOnTeamEntity> cachedEntities = Arrays.asList(
                createBusinessFunction("func1", "ADMIN", "TEAM"),
                createBusinessFunction("func2", "AGENT", "SELF")
            );
            Set<String> readonlyFunctions = Set.of("func3");

            when(permissionCacheRepository.findByAccountFriendlyNameCache(contactingContext, ACCOUNT_FRIENDLY_NAME))
                .thenReturn(cachedEntities);
            when(contactingConfigService.findByKey("BUSINESS_FUNCTIONS_HIDDEN"))
                .thenReturn(readonlyFunctions);

            // When
            List<BusinessFunctionVO> result = permissionService.getEditableBusinessFunctions(
                contactingContext,
                ACCOUNT_FRIENDLY_NAME
            );

            // Then
            assertThat(result)
                .hasSize(2)
                .extracting("businessFunction")
                .containsExactly("func1", "func2");
        }

        @Test
        @DisplayName("Should return empty list when cache returns no data")
        void shouldReturnEmptyListWhenCacheReturnsNoData() {
            // Given
            when(permissionCacheRepository.findByAccountFriendlyNameCache(contactingContext, ACCOUNT_FRIENDLY_NAME))
                .thenReturn(Collections.emptyList());
            when(contactingConfigService.findByKey("BUSINESS_FUNCTIONS_HIDDEN"))
                .thenReturn(Collections.emptySet());

            // When
            List<BusinessFunctionVO> result = permissionService.getEditableBusinessFunctions(
                contactingContext,
                ACCOUNT_FRIENDLY_NAME
            );

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("syncBusinessFunctions tests")
    class SyncBusinessFunctionsTests {

        private AccountDto accountDto;

        @BeforeEach
        void setUp() {
            accountDto = new AccountDto(1L, "sid", ACCOUNT_FRIENDLY_NAME, "ws-123", "UTC", 1, "LE", null, null);
            auditContext = mock(AuditContext.class);
        }

        @Test
        @DisplayName("Should throw exception for invalid business function")
        void shouldThrowExceptionForInvalidBusinessFunction() {
            // Given
            List<BusinessFunctionVO> newFunctions = List.of(
                new BusinessFunctionVO(ACCOUNT_FRIENDLY_NAME, "invalid-func", "ADMIN", TEAM, -1)
            );

            List<ContactingConfigEntity> configs = List.of(
                new ContactingConfigEntity("BUSINESS_FUNCTIONS", "func1,func2")
            );

            when(contactingConfigService.findAll()).thenReturn(configs);

            // When & Then
            assertThatThrownBy(() -> permissionService.syncBusinessFunctions(
                contactingContext,
                newFunctions,
                accountDto,
                auditContext
            )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalid business function");
        }

        @Test
        void fetchPermissions_employeeContext_returnsEmployeeVO() {
            EmployeeContext employeeContext = mock(EmployeeContext.class);
            when(employeeContext.employeeId()).thenReturn("emp1");
            List<BusinessFunctionVO> businessFunctions = List.of(
                    new BusinessFunctionVO("acc", "func", "role", OrganisationalRestrictionLevel.SELF, -1)
            );
            PermissionService spyService = spy(permissionService);
            doReturn(businessFunctions).when(spyService).getAllBusinessFunctions(contactingContext, "acc");
            doReturn( PermissionTestData.getNonEmployeePermissionMap()).when(spyService)
                    .getPermissionsForEmployeeContext("emp1", "acc", businessFunctions);

            Map<String, Object> result = spyService.fetchPermissions(employeeContext, contactingContext, "acc");
            assertTrue(result.containsKey("businessFunctions"));
            assertThat(result).isInstanceOf(Map.class);
        }

        @Test
        void fetchPermissions_customerContext_returnsNonEmployeeVO() {
            CustomerContext customerContext = mock(CustomerContext.class);
            List<BusinessFunctionVO> businessFunctions = List.of();
            PermissionService spyService = spy(permissionService);
            doReturn(businessFunctions).when(spyService).getAllBusinessFunctions(contactingContext, "acc");
            doReturn(PermissionTestData.getNonEmployeePermissionMap()).when(spyService)
                    .createNonEmployeePermissionVO(businessFunctions, Set.of("CUSTOMER_AUTHENTICATED"));

            Map<String, Object> result = spyService.fetchPermissions(customerContext, contactingContext, "acc");
            assertTrue(result.containsKey("businessFunctions"));
            assertThat(result).isInstanceOf(Map.class);
        }

        @Test
        void fetchPermissions_foreignApiContext_returnsNonEmployeeVO() {
            ForeignApiContext foreignApiContext = mock(ForeignApiContext.class);
            List<BusinessFunctionVO> businessFunctions = List.of();
            PermissionService spyService = spy(permissionService);
            doReturn(businessFunctions).when(spyService).getAllBusinessFunctions(contactingContext, "acc");
            doReturn(PermissionTestData.getNonEmployeePermissionMap()).when(spyService)
                    .createNonEmployeePermissionVO(businessFunctions, Set.of("FOREIGN_API"));

            Map<String, Object> result = spyService.fetchPermissions(foreignApiContext, contactingContext, "acc");
            assertTrue(result.containsKey("businessFunctions"));
            assertThat(result).isInstanceOf(Map.class);
        }

        @Test
        void fetchPermissions_default_returnsNonEmployeeVO() {
            AuthorizationContext unknownContext = mock(AuthorizationContext.class);
            List<BusinessFunctionVO> businessFunctions = List.of();
            PermissionService spyService = spy(permissionService);
            doReturn(businessFunctions).when(spyService).getAllBusinessFunctions(contactingContext, "acc");
            doReturn(PermissionTestData.getNonEmployeePermissionMap()).when(spyService)
                    .createNonEmployeePermissionVO(businessFunctions, Set.of("CUSTOMER_UNAUTHENTICATED"));

            Map<String, Object> result = spyService.fetchPermissions(unknownContext, contactingContext, "acc");
            assertTrue(result.containsKey("businessFunctions"));
            assertThat(result).isInstanceOf(Map.class);
        }
    }

    private BusinessFunctionOnTeamEntity createBusinessFunction(String name, String role, String restriction) {
        BusinessFunctionOnTeamEntity entity = new BusinessFunctionOnTeamEntity();
        entity.setAccountFriendlyName(ACCOUNT_FRIENDLY_NAME);
        entity.setBusinessFunction(name);
        entity.setRole(role);
        entity.setRestriction(restriction);
        entity.setOrganisationId(-1);
        return entity;
    }
}