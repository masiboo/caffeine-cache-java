package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.connection.ConnectionModel;
import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import jakarta.ws.rs.core.Response;
import nl.ing.api.contacting.conf.helper.AccountsTestData;
import nl.ing.api.contacting.conf.helper.ConnectionTestData;
import nl.ing.api.contacting.conf.resource.dto.ConnectionModelV1;
import nl.ing.api.contacting.conf.resource.dto.TwilioRegionDTO;
import nl.ing.api.contacting.conf.service.ConnectionDetailsServiceJava;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConnectionResourceJavaTest {

    @Mock
    private ConnectionDetailsServiceJava connectionDetailsService;



    @InjectMocks
    private TestableConnectionResourceJava resource;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        resource = new TestableConnectionResourceJava(connectionDetailsService);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Nested
    @DisplayName("GetConnections")
    class GetConnections {
        @Test
        @DisplayName("getAllConnections returns 200 and mapped DTOs on success")
        void getAllConnectionsReturnSuccess() {
            List<ConnectionModelV1.Connection> dtos = List.of(ConnectionTestData.getConnectionV1());

            when(connectionDetailsService.getAllConnections()).thenReturn(new ConnectionModelV1.AllConnections(dtos));
            var future = resource.getAllConnections();
            Response response = future.join();
            assertEquals(200, response.getStatus());
            // Assert the entity type and its content
            assertInstanceOf(ConnectionModelV1.AllConnections.class, response.getEntity());
            ConnectionModelV1.AllConnections allConnections = (ConnectionModelV1.AllConnections) response.getEntity();
            assertEquals(1, allConnections.connections().size());
            ConnectionModelV1.Connection connection = allConnections.connections().get(0);
            assertEquals(1001L, connection.accountId());
            assertFalse(connection.backend().isEmpty());
            assertEquals("domainName", connection.backend().get(0).domain());
            verify(connectionDetailsService).getAllConnections();
        }


        @Test
        @DisplayName("getAllConnections returns on mapping error")
        void getAllConnectionsReturnsMappingError() {
            when(connectionDetailsService.getAllConnections()).thenThrow(new RuntimeException("Mapping error"));
            Response response = resource.getAllConnections().join();
            assertEquals(500, response.getStatus());
            assertThat(response.getEntity().toString()).contains("An unexpected error occurred");
        }

        @Test
        @DisplayName("getAllConnections returns on service exception")
        void getAllConnectionsReturnsServiceException() {
            when(connectionDetailsService.getAllConnections()).thenThrow(new RuntimeException("Service error"));
            Response response = resource.getAllConnections().join();
            assertEquals(500, response.getStatus());
            assertThat(response.getEntity().toString()).contains("An unexpected error occurred");
        }

        @Test
        @DisplayName("getAllConnectionForAccount returns and mapped DTOs on success")
        void getAllConnectionForAccountReturnsSuccess() {
            ContactingContext contactingContext = resource.getContactingContext();
            ConnectionModel.Connection dtos = ConnectionTestData.getConnection();
            when(connectionDetailsService.getAllConnectionsForAccount(Mockito.any())).thenReturn(dtos);
            Response response = resource.getAllConnectionForAccount(false).join();
            assertEquals(200, response.getStatus());
            assertInstanceOf(ConnectionModel.Connection.class, response.getEntity());

            ConnectionModel.Connection connection = (ConnectionModel.Connection) response.getEntity();
            assertNotNull(connection);

            assertEquals(contactingContext.accountId(), connection.accountId());
            assertFalse(connection.backend().isEmpty());
            assertFalse(connection.frontend().isEmpty());

            // Backend assertions
            ConnectionModel.TwilioDomain backendDomain = connection.backend().get(0);
            assertEquals("domainName", backendDomain.domain());
            assertEquals(ConnectionType.PRIMARY, backendDomain.active());
            assertTrue(backendDomain.urls().primary().id().isPresent());
            assertTrue(backendDomain.urls().primary().region().isPresent());
            assertEquals("https://primary.test.com", backendDomain.urls().primary().url());

            // Frontend assertions
            ConnectionModel.TwilioDomain frontendDomain = connection.frontend().get(0);
            assertEquals("domainName", frontendDomain.domain());
            assertEquals(ConnectionType.PRIMARY, frontendDomain.active());
            assertTrue(frontendDomain.urls().primary().id().isPresent());
            assertEquals("https://primary.test.com", frontendDomain.urls().primary().url());

            // URLs assertions
            ConnectionModel.URLs urls = backendDomain.urls();
            assertEquals("Leeuwarden", urls.primary().edgeLocation());
            assertTrue(urls.fallback().isPresent());
            assertEquals("Amsterdam", urls.fallback().get().edgeLocation());
            assertTrue(urls.workFromHome().isPresent());
            assertEquals("Home Office", urls.workFromHome().get().edgeLocation());

            verify(connectionDetailsService).getAllConnectionsForAccount(Mockito.any());
        }


        @Test
        @DisplayName("getAllConnectionForAccount returns on mapping error")
        void getAllConnectionForAccountReturnsMappingError() {
            when(connectionDetailsService.getAllConnections()).thenThrow(new RuntimeException("Mapping error"));
            Response response = resource.getAllConnections().join();
            assertEquals(500, response.getStatus());
            assertThat(response.getEntity().toString()).contains("An unexpected error occurred");
        }

        @Test
        @DisplayName("getAllConnectionForAccount returns on service exception")
        void getAllConnectionForAccountReturnsServiceException() {
            when(connectionDetailsService.getAllConnectionsForAccount(Mockito.any())).thenThrow(new RuntimeException("Service error"));
            Response response = resource.getAllConnectionForAccount(false).join();
            assertEquals(500, response.getStatus());
            assertThat(response.getEntity().toString()).contains("An unexpected error occurred");
        }

        @Test
        @DisplayName("getAllBackend returns and mapped backend DTOs on success")
        void getAllBackendReturnsSuccess() {
            Long accountId = resource.getContactingContext().accountId();
            List<ConnectionModel.TwilioDomain> backend = List.of(ConnectionTestData.getTwilioDomain());
            when(connectionDetailsService.getAllBackend(accountId)).thenReturn(new ConnectionModel.BackendConnections(backend));
            Response response = resource.getAllBackend(accountId).join();
            assertEquals(200, response.getStatus());
            assertInstanceOf(ConnectionModel.BackendConnections.class, response.getEntity());
            ConnectionModel.BackendConnections backendConnections =
                    (ConnectionModel.BackendConnections) response.getEntity();
            assertNotNull(backendConnections.backend());
            assertEquals(1, backendConnections.backend().size());
            ConnectionModel.TwilioDomain domain = backendConnections.backend().get(0);
            assertEquals("domainName", domain.domain());
            assertEquals(ConnectionType.PRIMARY, domain.active());
            // URLs assertions
            ConnectionModel.URLs urls = domain.urls();
            assertNotNull(urls);
            assertEquals("Leeuwarden", urls.primary().edgeLocation());
            assertEquals("https://primary.test.com", urls.primary().url());
            assertTrue(urls.fallback().isPresent());
            assertEquals("Amsterdam", urls.fallback().get().edgeLocation());
            assertTrue(urls.workFromHome().isPresent());
            assertEquals("Home Office", urls.workFromHome().get().edgeLocation());
        }


        @Test
        @DisplayName("getAllBackend returns service exception")
        void getAllBackendReturnsServiceException() {
            Long accountId = resource.getContactingContext().accountId();
            when(connectionDetailsService.getAllBackend(accountId)).thenThrow(new RuntimeException("Service error"));
            Response response = resource.getAllBackend(accountId).join();
            assertEquals(500, response.getStatus());
            assertThat(response.getEntity().toString()).contains("An unexpected error occurred");
        }

        @Test
        @DisplayName("getAllFrontEnd returns mapped frontend DTOs on success")
        void getAllFrontEndReturnsSuccess() {
            Long accountId = resource.getContactingContext().accountId();
            List<ConnectionModel.TwilioDomain> frontend = List.of(ConnectionTestData.getTwilioDomain());

            when(connectionDetailsService.getAllFrontEnd(accountId)).thenReturn(new ConnectionModel.FrontendConnections(frontend));

            Response response = resource.getAllFrontEnd(accountId).join();

            assertEquals(200, response.getStatus());
            assertInstanceOf(ConnectionModel.FrontendConnections.class, response.getEntity());
            ConnectionModel.FrontendConnections frontendConnections =
                    (ConnectionModel.FrontendConnections) response.getEntity();
            assertNotNull(frontendConnections.frontend());
            assertEquals(1, frontendConnections.frontend().size());
            ConnectionModel.TwilioDomain domain = frontendConnections.frontend().get(0);
            assertEquals("domainName", domain.domain());
            assertEquals(ConnectionType.PRIMARY, domain.active());
            // URLs assertions
            ConnectionModel.URLs urls = domain.urls();
            assertNotNull(urls);
            assertEquals("Leeuwarden", urls.primary().edgeLocation());
            assertEquals("https://primary.test.com", urls.primary().url());
            assertTrue(urls.fallback().isPresent());
            assertEquals("Amsterdam", urls.fallback().get().edgeLocation());
            assertEquals("ie1", urls.fallback().get().region().get());
            assertTrue(urls.workFromHome().isPresent());
            assertEquals("Home Office", urls.workFromHome().get().edgeLocation());
        }

        @Test
        @DisplayName("getAllFrontEnd returns 500 on service exception")
        void getAllFrontEndReturnsServiceException() {
            Long accountId = 1234L;
            when(connectionDetailsService.getAllFrontEnd(accountId))
                    .thenThrow(new RuntimeException("Service error"));
            Response response = resource.getAllFrontEnd(accountId).join();
            assertEquals(500, response.getStatus());
            assertThat(response.getEntity().toString()).contains("An unexpected error occurred");
        }


        @Test
        @DisplayName("getAllRegions returns all regions DTO")
        void getAllRegionsReturnsAllRegions() {
            TwilioRegionDTO.Regions result = resource.getAllRegions().join();
            assertThat(result.regions().get(0).name()).isEqualTo("ie1");
            assertThat(result.regions().get(1).name()).isEqualTo("au1");
            assertThat(result.regions().get(2).name()).isEqualTo("us1");

        }
    }

    static class TestableConnectionResourceJava extends ConnectionResourceJava {
        TestableConnectionResourceJava(ConnectionDetailsServiceJava connectionDetailsService) {
            super(connectionDetailsService);
        }

        @Override
        protected ContactingContext getContactingContext() {
            return AccountsTestData.getContactingContext();
        }
    }
}
