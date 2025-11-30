package nl.ing.api.contacting.conf.resource.jaxrs.support;


import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.resource.account.AccountDto;
import jakarta.ws.rs.container.ContainerRequestContext;
import nl.ing.api.contacting.conf.business.context.ContactingContextProviderJava;
import nl.ing.api.contacting.conf.exception.ApplicationEsperantoException;
import nl.ing.api.contacting.trust.rest.context.SessionContext;
import nl.ing.api.contacting.trust.rest.context.SessionContextAttributesHelper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseResourceJavaTest {

    @Mock
    private ContainerRequestContext mockContainerRequest;

    @Mock
    private SessionContext mockSessionContext;

    @Mock
    private AccountDto mockAccount;
    private TestableBaseResource resource;

    @BeforeEach
    void setUp() {
        resource = new TestableBaseResource(mockContainerRequest);
    }

    @Nested
    @DisplayName("getSessionContext")
    class GetSessionContext {

        @Test
        void shouldReturnEmptyOptionalIfNoSession() {
            try (MockedStatic<SessionContextAttributesHelper> mockedStatic =
                         mockStatic(SessionContextAttributesHelper.class)) {
                mockedStatic.when(
                        () -> SessionContextAttributesHelper.getSessionContext(any(), any())
                ).thenReturn(Optional.empty());

                Optional<SessionContext> result = resource.getSessionContext();
                assertTrue(result.isEmpty());
            }
        }

        @Test
        void shouldReturnSessionContextIfPresent() {
            try (MockedStatic<SessionContextAttributesHelper> mockedStatic =
                         mockStatic(SessionContextAttributesHelper.class)) {
                mockedStatic.when(
                        () -> SessionContextAttributesHelper.getSessionContext(any(), any())
                ).thenReturn(Optional.of(mockSessionContext));
                Optional<SessionContext> result = resource.getSessionContext();
                assertTrue(result.isPresent());
                assertEquals(mockSessionContext, result.get());
            }
        }
    }

    @Nested
    @DisplayName("getAccount")
    class GetAccount {
        @Test
        void shouldReturnAccountIfSubAccountPresent() {
            try (MockedStatic<SessionContextAttributesHelper> mockedStatic =
                         mockStatic(SessionContextAttributesHelper.class)) {
                mockedStatic.when(
                        () -> SessionContextAttributesHelper.getSessionContext(any(), any())
                ).thenReturn(Optional.of(mockSessionContext));
                when(mockSessionContext.getSubAccount()).thenReturn(Optional.of(mockAccount));
                AccountDto account = resource.account();
                assertEquals(mockAccount, account);
            }
        }

        @Test
        void shouldThrowNotFoundIfNoSubAccount() {
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> resource.account());
            assertTrue(ex.getMessage().contains("Request has no subAccount"));
        }
    }

    @Nested
    @DisplayName("getContactingContext")
    class GetContactingContext {
        @Test
        void shouldReturnContactingContextWhenSessionAndAccountPresent() {
            TestableBaseResource spyResource = spy(resource);
            doReturn(Optional.of(mockSessionContext)).when(spyResource).getSessionContext();
            doReturn(mockAccount).when(spyResource).account();
            // Mock static method of ContactingContextProviderJava
            ContactingContext mockContext = mock(ContactingContext.class);
            try (MockedStatic<ContactingContextProviderJava> mockedStatic =
                         mockStatic(ContactingContextProviderJava.class)) {
                mockedStatic.when(
                        () -> ContactingContextProviderJava.getContactingContext(mockAccount, Optional.ofNullable(mockSessionContext))
                ).thenReturn(mockContext);
                ContactingContext result = spyResource.getContactingContext();
                assertEquals(mockContext, result);
            }
        }

        @Test
        void shouldThrowIfSessionMissing() {
            try (MockedStatic<SessionContextAttributesHelper> mockedStatic =
                         mockStatic(SessionContextAttributesHelper.class)) {
                mockedStatic.when(
                        () -> SessionContextAttributesHelper.getSessionContext(any(), any())
                ).thenReturn(Optional.empty());
                ApplicationEsperantoException ex = assertThrows(ApplicationEsperantoException.class, () -> resource.getContactingContext());
                assertTrue(ex.getMessage().contains("Request has no SessionContext"));
            }
        }

    }

    /**

     * Concrete subclass to access protected methods.

     */
    static class TestableBaseResource extends BaseResourceJava {
        TestableBaseResource(ContainerRequestContext containerRequest) {
            this.containerRequest = containerRequest;
        }
    }

}

