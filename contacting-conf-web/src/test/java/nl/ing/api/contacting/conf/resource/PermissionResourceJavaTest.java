package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.organisation.BusinessFunctionsDto;
import com.ing.api.contacting.dto.resource.account.AccountDto;
import jakarta.ws.rs.core.Response;
import nl.ing.api.contacting.conf.domain.model.permission.BusinessFunctionVO;
import nl.ing.api.contacting.conf.domain.model.permission.BusinessFunctionsDtoWrapper;
import nl.ing.api.contacting.conf.domain.model.permission.PermissionBusinessFunctionVO;
import nl.ing.api.contacting.conf.exception.ApplicationEsperantoException;
import nl.ing.api.contacting.conf.helper.PermissionTestData;
import nl.ing.api.contacting.conf.service.PermissionService;
import nl.ing.api.contacting.trust.rest.context.APISystemContext;
import nl.ing.api.contacting.trust.rest.context.AuthorizationContext;
import nl.ing.api.contacting.trust.rest.context.SessionContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static nl.ing.api.contacting.conf.domain.model.permission.OrganisationalRestrictionLevel.SELF;
import static nl.ing.api.contacting.conf.domain.model.permission.OrganisationalRestrictionLevel.TEAM;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionResourceJavaTest {

    @Mock
    private PermissionService mockPermissionService;
    @Mock
    private SessionContext sessionContext;
    @Mock
    private AuthorizationContext authContext;
    @Mock
    private APISystemContext apiContext;

    private PermissionResourceJava resource;
    public static final String ACCOUNT_FRIENDLY_NAME = "test-account";
    private static final String EMPLOYEE_ID = "emp123";

    @BeforeEach
    void setUp() {
        resource = new TestablePermissionResourceJava(mockPermissionService);
    }

    @Nested
    @DisplayName("getPermissionsV2")
    class GetPermissionsV2Tests {

        private AccountDto accountDto;

        @BeforeEach
        void setUp() {
            accountDto = new AccountDto(1L, "sid", ACCOUNT_FRIENDLY_NAME, "ws-123", "UTC", 1, "LE", null, null);
        }

        @Test
        @DisplayName("should return permissions for authorization context")
        void shouldReturnPermissionsForAuthContext() throws Exception {
            // Given
            when(sessionContext.trustContext()).thenReturn(authContext);
            Map<String, Object> expectedPermissions = PermissionTestData.getResponseMap();

            when(mockPermissionService.fetchPermissions(eq(authContext), any(ContactingContext.class), eq(ACCOUNT_FRIENDLY_NAME)))
                .thenReturn(expectedPermissions);

            // When
            CompletableFuture<Response> responseFuture = resource.getPermissionsV2(sessionContext);
            Response response = responseFuture.get();

            // Then
            assertEquals(200, response.getStatus());
            HashMap<String, Object> result = (HashMap<String, Object>) response.getEntity();
            assertThat(result)
                .isNotNull()
                .isInstanceOf(HashMap.class);
        }

        @Test
        @DisplayName("should return system tooling permission for API context")
        void shouldReturnSystemToolingPermissionForApiContext() throws Exception {
            // Given
            when(sessionContext.trustContext()).thenReturn(apiContext);
            when(apiContext.apiName()).thenReturn("test-api");

            // When
            CompletableFuture<Response> responseFuture = resource.getPermissionsV2(sessionContext);
            Response response = responseFuture.get();

            // Then
            assertEquals(200, response.getStatus());
            assertEquals(PermissionService.systemToolingPermission, response.getEntity());
        }
    }

    @Nested
    @DisplayName("getPermissions")
    class GetPermissionsTests {

        @Test
        @DisplayName("should return permissions for employee")
        void shouldReturnPermissionsForEmployee() throws Exception {
            // Given
            Map<String, Object> expectedPermissions = Map.of("role", "ADMIN", "functions", List.of("func1", "func2"));
            when(mockPermissionService.getPermissionsForEmployeeAndAccountFriendlyName(
                any(ContactingContext.class), eq(EMPLOYEE_ID), eq(ACCOUNT_FRIENDLY_NAME)))
                .thenReturn(expectedPermissions);

            // When
            CompletableFuture<Response> responseFuture = resource.getPermissions(EMPLOYEE_ID, ACCOUNT_FRIENDLY_NAME);
            Response response = responseFuture.get();

            // Then
            assertEquals(200, response.getStatus());
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) response.getEntity();
            assertThat(result).toString();
        }
    }

    @Nested
    @DisplayName("updatePermissions")
    class UpdatePermissionsTests {

        private List<BusinessFunctionsDto> permissions;
        private AccountDto accountDto;

        @BeforeEach
        void setUp() {
            permissions = PermissionTestData.getBusinessFunctionsDto();
            accountDto = new AccountDto(1L, "sid", ACCOUNT_FRIENDLY_NAME, "ws-123", "UTC", 1, "LE", null, null);
        }

        @Test
        @DisplayName("should handle sync return 204 and null entity")
        void shouldHandleSyncFailure() throws ExecutionException, InterruptedException {

            Response response = resource.updatePermissions(permissions, sessionContext, false).get();

            // Then
            Assertions.assertEquals(204, response.getStatus());
            Assertions.assertNull(response.getEntity());
        }
    }

    @Nested
    @DisplayName("getAllBusinessFunctions")
    class GetAllBusinessFunctionsTests {

        @Test
        @DisplayName("should return all business functions")
        void shouldReturnAllBusinessFunctions() throws Exception {
            // Given
            List<BusinessFunctionVO> businessFunctions = List.of(
                new BusinessFunctionVO(ACCOUNT_FRIENDLY_NAME, "func1", "ADMIN", TEAM, -1),
                new BusinessFunctionVO(ACCOUNT_FRIENDLY_NAME, "func2", "AGENT", SELF, -1)
            );
            when(mockPermissionService.getEditableBusinessFunctions(any(ContactingContext.class), eq(ACCOUNT_FRIENDLY_NAME)))
                .thenReturn(businessFunctions);

            // When
            CompletableFuture<Response> responseFuture = resource.getAllBusinessFunctions(sessionContext);
            Response response = responseFuture.get();
            BusinessFunctionsDtoWrapper wrapper = (BusinessFunctionsDtoWrapper) response.getEntity();

            // Then
            Assertions.assertEquals(200, response.getStatus());
            Assertions.assertEquals("func1", wrapper.data().get(0).name());
        }


        @Test
        @DisplayName("should throw not found exception when no permissions exist")
        void shouldThrowNotFoundWhenNoPermissions() {
            // Given
            when(mockPermissionService.getEditableBusinessFunctions(any(ContactingContext.class), eq(ACCOUNT_FRIENDLY_NAME)))
                .thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> resource.getAllBusinessFunctions(sessionContext).get())
                .isInstanceOf(ApplicationEsperantoException.class)
                .hasMessage("no permissions found");
        }


    }

    static class TestablePermissionResourceJava extends PermissionResourceJava {
        TestablePermissionResourceJava(PermissionService service) {
            super(service);
        }

        @Override
        protected ContactingContext getContactingContext() {
            return new ContactingContext(101L, null);
        }

        @Override
        protected AccountDto accountFromRequestContext(SessionContext sessionContext) {
            return new AccountDto(1L, "sid", ACCOUNT_FRIENDLY_NAME, "ws-123", "UTC", 1, "LE", null, null);
        }
    }

    private PermissionBusinessFunctionVO createEmployeeBusinessFunctionVO() {
        return  PermissionTestData.getEmployeeBusinessFunctionVO();

    }
}