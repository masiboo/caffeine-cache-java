package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.connection.ConnectionDetailsDtoJava;
import com.ing.api.contacting.dto.java.resource.connection.ConnectionModel;
import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import com.ing.api.contacting.dto.java.resource.connection.Layer;
import nl.ing.api.contacting.conf.domain.entity.ConnectionDetailsEntity;
import nl.ing.api.contacting.conf.domain.entity.ConnectionEntity;
import nl.ing.api.contacting.conf.domain.model.connection.*;
import nl.ing.api.contacting.conf.exception.ApplicationEsperantoException;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.repository.ConnectionDetailsCacheRepository;
import nl.ing.api.contacting.conf.repository.ConnectionDetailsJpaRepository;
import nl.ing.api.contacting.conf.resource.dto.ConnectionModelV1;
import nl.ing.api.java.contacting.caching.core.ContactingCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionDetailsServiceJavaTest {

    @Mock
    private ConnectionDetailsJpaRepository connectionDetailsRepository;

    @Mock
    private ConnectionDetailsCacheRepository connectionDetailsCacheRepository;

    @InjectMocks
    private ConnectionDetailsServiceJava connectionDetailsService;

    @Mock
    private WebhookConnectionService webhookConnectionService;

    @Mock
    private ContactingCache contactingCache;

    @Mock
    private IpAddressServiceJava ipAddressServiceJava;

    @Mock
    public ActiveConnectionServiceJava activeConnectionService;

    private AutoCloseable mocks;

    private ContactingContext contactingContext;

    @BeforeEach
    void setUp() {
        contactingContext = new ContactingContext(101L, null);
    }

    @Test
    @DisplayName("getByAccountAndLayer retrieves connections for a specific account and layer with dummy values")
    void testGetByAccountAndLayer() {
        Long accountId = 1L;
        Layer layer = Layer.BACKEND;

        ConnectionEntity connectionEntity = ConnectionEntity.builder()
                .id(1L)
                .accountId(accountId)
                .layer(layer.getValue())
                .domain("test-domain")
                .build();

        ConnectionDetailsEntity detailsEntity = ConnectionDetailsEntity.builder()
                .id(10L)
                .connectionType(ConnectionType.PRIMARY.getValue())
                .edgeLocation("edge-1")
                .url("https://dummy.url")
                .region("eu-west")
                .build();

        ConnectionDetailsDTO dto = new ConnectionDetailsDTO(detailsEntity, connectionEntity, null);
        when(connectionDetailsRepository.getByAccountAndLayer(accountId, layer.getValue())).thenReturn(List.of(dto));

        List<ConnectionWithDetails> result =  connectionDetailsService.getByAccountAndLayer(accountId, layer);

        assertNotNull(result);
        assertEquals(1, result.size());

        ConnectionWithDetails actual = result.get(0);
        assertNotNull(actual.connectionVO());
        assertNotNull(actual.connectionDetailsVO());

        // ConnectionVO assertions
        assertEquals(1L, actual.connectionVO().id());
        assertEquals(accountId, actual.connectionVO().accountId());
        assertEquals(layer, actual.connectionVO().layer());
        assertEquals("test-domain", actual.connectionVO().domain());

        // ConnectionDetailsVO assertions
        assertEquals(10L, actual.connectionDetailsVO().id());
        assertEquals(1L, actual.connectionDetailsVO().connectionId());
        assertEquals(ConnectionType.PRIMARY, actual.connectionDetailsVO().connectionType());
        assertEquals("edge-1", actual.connectionDetailsVO().edgeLocation());
        assertEquals("https://dummy.url", actual.connectionDetailsVO().url());
        assertEquals("eu-west", actual.connectionDetailsVO().region());

        // Active flag
        assertFalse(actual.active());

        verify(connectionDetailsRepository, times(1)).getByAccountAndLayer(accountId, layer.getValue());
    }

    @Test
    @DisplayName("getAll retrieves all active connections with dummy values")
    void testGetAll() {
        ConnectionEntity connectionEntity = ConnectionEntity.builder()
                .id(2L)
                .accountId(2L)
                .layer(Layer.FRONTEND.getValue())
                .domain("all-domain")
                .build();

        ConnectionDetailsEntity detailsEntity = ConnectionDetailsEntity.builder()
                .id(20L)
                .connectionType(ConnectionType.FALLBACK.getValue())
                .edgeLocation("edge-2")
                .url("https://all.url")
                .region("us-east")
                .build();

        ConnectionDetailsDTO dto = new ConnectionDetailsDTO(detailsEntity, connectionEntity, null);
        when(connectionDetailsRepository.findAllData()).thenReturn(List.of(dto));

        List<ConnectionWithDetails> result = connectionDetailsService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());

        ConnectionWithDetails actual = result.get(0);
        assertNotNull(actual.connectionVO());
        assertNotNull(actual.connectionDetailsVO());

        // ConnectionVO assertions
        assertEquals(2L, actual.connectionVO().id());
        assertEquals(2L, actual.connectionVO().accountId());
        assertEquals(Layer.FRONTEND, actual.connectionVO().layer());
        assertEquals("all-domain", actual.connectionVO().domain());

        // ConnectionDetailsVO assertions
        assertEquals(20L, actual.connectionDetailsVO().id());
        assertEquals(2L, actual.connectionDetailsVO().connectionId());
        assertEquals(ConnectionType.FALLBACK, actual.connectionDetailsVO().connectionType());
        assertEquals("edge-2", actual.connectionDetailsVO().edgeLocation());
        assertEquals("https://all.url", actual.connectionDetailsVO().url());
        assertEquals("us-east", actual.connectionDetailsVO().region());

        // Active flag
        assertFalse(actual.active());

        verify(connectionDetailsRepository, times(1)).findAllData();
    }

    @Test
    @DisplayName("getAllByAccountId retrieves connections for a specific account with dummy values")
    void testGetAllByAccountId() {
        Long accountId = 3L;
        ContactingContext context = new ContactingContext(3L, null);
        ConnectionEntity connectionEntity = ConnectionEntity.builder()
                .id(3L)
                .accountId(accountId)
                .layer(Layer.BACKEND.getValue())
                .domain("by-account-domain")
                .build();

        ConnectionDetailsEntity detailsEntity = ConnectionDetailsEntity.builder()
                .id(30L)
                .connectionType(ConnectionType.WORK_FROM_HOME.getValue())
                .edgeLocation("edge-3")
                .url("https://byaccount.url")
                .region("apac")
                .build();

        ConnectionDetailsDTO dto = new ConnectionDetailsDTO(detailsEntity, connectionEntity, null);
        when(connectionDetailsCacheRepository.findAllForAccount(context)).thenReturn(List.of(dto));

        List<ConnectionWithDetails> result = connectionDetailsService.getAllByAccountId(context);

        assertNotNull(result);
        assertEquals(1, result.size());

        ConnectionWithDetails actual = result.get(0);
        assertNotNull(actual.connectionVO());
        assertNotNull(actual.connectionDetailsVO());

        // ConnectionVO assertions
        assertEquals(3L, actual.connectionVO().id());
        assertEquals(accountId, actual.connectionVO().accountId());
        assertEquals(Layer.BACKEND, actual.connectionVO().layer());
        assertEquals("by-account-domain", actual.connectionVO().domain());

        // ConnectionDetailsVO assertions
        assertEquals(30L, actual.connectionDetailsVO().id());
        assertEquals(3L, actual.connectionDetailsVO().connectionId());
        assertEquals(ConnectionType.WORK_FROM_HOME, actual.connectionDetailsVO().connectionType());
        assertEquals("edge-3", actual.connectionDetailsVO().edgeLocation());
        assertEquals("https://byaccount.url", actual.connectionDetailsVO().url());
        assertEquals("apac", actual.connectionDetailsVO().region());

        // Active flag
        assertFalse(actual.active());

        verify(connectionDetailsCacheRepository, times(1)).findAllForAccount(context);
    }

    @Test
    @DisplayName("getAllByAccountId: negative scenario returns empty list")
    void getAllByAccountIdWithEmpty() {
      //  when(connectionDetailsRepository.findAllDataByAccountId(contactingContext.accountId())).thenReturn(List.of());
        ConnectionModel.Connection result = connectionDetailsService.getAllConnectionsForAccount(contactingContext);
        assertNotNull(result);
        assertTrue(result.backend().isEmpty());
        assertTrue(result.frontend().isEmpty());
        assertTrue(result.webhooks().isEmpty());
    }

    @Test
    @DisplayName("getAllConnectionsForAccount returns all connections and webhooks for account")
    void getAllConnectionsForAccount_returnsAllConnections() {
        var context = new ContactingContext(101L, null);

        ConnectionEntity connectionEntity = ConnectionEntity.builder()
                .id(3L)
                .accountId(context.accountId())
                .layer(Layer.BACKEND.getValue())
                .domain("by-account-domain")
                .build();

        ConnectionDetailsEntity detailsEntity = ConnectionDetailsEntity.builder()
                .id(30L)
                .connectionType(ConnectionType.PRIMARY.getValue())
                .edgeLocation("edge-3")
                .url("https://byaccount.url")
                .region("apac")
                .build();

        ConnectionDetailsDTO dto = new ConnectionDetailsDTO(detailsEntity, connectionEntity, null);


        when(connectionDetailsCacheRepository.findAllForAccount(context)).thenReturn(List.of(dto));

        var webhooks = List.of(new WebhookConnectionVO(
                200L,
                ConnectionType.PRIMARY,
                "https://webhook.example.com",
                context.accountId(),
                true
        ));
        when(webhookConnectionService.getAllByAccountId(context.accountId())).thenReturn(webhooks);
        ConnectionModel.Connection result = connectionDetailsService.getAllConnectionsForAccount(context);
        assertNotNull(result);
        assertEquals(context.accountId(), result.accountId());
        assertEquals(1, result.backend().size());
        assertEquals(0, result.frontend().size());
        assertTrue(result.webhooks().isPresent());
        ConnectionModel.Webhook webhook = result.webhooks().get();
        assertEquals(ConnectionType.PRIMARY, webhook.active());
        assertEquals("https://webhook.example.com", webhook.urls().primary().url());
        ConnectionModel.TwilioDomain backend = result.backend().get(0);
        Long id = backend.id().isPresent() ? backend.id().get() : null;
        assertEquals(3L, id);
        assertEquals("by-account-domain", backend.domain());
        assertEquals("https://byaccount.url", backend.urls().primary().url());
        verify(webhookConnectionService, times(1)).getAllByAccountId(context.accountId());
    }

    @Test
    @DisplayName("getAgentConnectionSettings returns correct settings for account and IP")
    void getAgentConnectionSettings_returnsExpectedSettings() {
        var context = new ContactingContext(101L, null);
        var ipAddresses = "192.168.1.1";
        var wfhIndicator = Boolean.TRUE;

        var frontendConnection = new ConnectionWithDetails(
                new nl.ing.api.contacting.conf.domain.model.connection.ConnectionVO(
                        1L,
                        101L,
                        Layer.FRONTEND,
                        "frontend-domain"
                ),
                new nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsVO(
                        10L,
                        1L,
                        com.ing.api.contacting.dto.java.resource.connection.ConnectionType.PRIMARY,
                        "edge-1",
                        "https://frontend.url",
                        "eu-west"
                ),
                true
        );

        when(ipAddressServiceJava.fetchWFHIndicator(ipAddresses, context)).thenReturn(wfhIndicator);
        when(connectionDetailsRepository.getByAccountAndLayer(101L, Layer.FRONTEND.getValue()))
                .thenReturn(List.of(new ConnectionDetailsDTO(
                        ConnectionDetailsEntity.builder()
                                .id(frontendConnection.connectionDetailsVO().id())
                                .id(frontendConnection.connectionDetailsVO().connectionId())
                                .connectionType(frontendConnection.connectionDetailsVO().connectionType().getValue())
                                .edgeLocation(frontendConnection.connectionDetailsVO().edgeLocation())
                                .url(frontendConnection.connectionDetailsVO().url())
                                .region(frontendConnection.connectionDetailsVO().region())
                                .build(),
                        ConnectionEntity.builder()
                                .id(frontendConnection.connectionVO().id())
                                .accountId(frontendConnection.connectionVO().accountId())
                                .layer(frontendConnection.connectionVO().layer().getValue())
                                .domain(frontendConnection.connectionVO().domain())
                                .build(),
                        null
                )));

        AgentConnectionSettings result = connectionDetailsService.getAgentConnectionSettings(context, ipAddresses);

        assertThat(result).isNotNull();
        assertThat(result.wfhIndicator()).isTrue();
        assertThat(result.frontendConnections()).isNotNull();
        assertEquals(1,result.frontendConnections().frontend().size());
        verify(ipAddressServiceJava).fetchWFHIndicator(ipAddresses, context);
        verify(connectionDetailsRepository).getByAccountAndLayer(101L, Layer.FRONTEND.getValue());
    }

    @Test
    @DisplayName("getAllConnectionsV2 returns all connections and webhooks")
    void getAllConnectionsV2_returnsAllConnections() {
        var connections = new ConnectionWithDetails(
                new nl.ing.api.contacting.conf.domain.model.connection.ConnectionVO(
                        1L,
                        101L,
                        Layer.FRONTEND,
                        "frontend-domain"
                ),
                new nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsVO(
                        10L,
                        1L,
                        com.ing.api.contacting.dto.java.resource.connection.ConnectionType.PRIMARY,
                        "edge-1",
                        "https://frontend.url",
                        "eu-west"
                ),
                true
        );
        var webhooks = List.of(new WebhookConnectionVO(
                200L,
                ConnectionType.PRIMARY,
                "https://webhook.example.com",
                1L,
                true
        ));

        when(activeConnectionService.getAll()).thenReturn(Collections.singletonList(connections));
        when(webhookConnectionService.getAll()).thenReturn(webhooks);

        ConnectionModel.AllConnections result = connectionDetailsService.getAllConnectionsV2();

        assertThat(result).isNotNull();
        verify(activeConnectionService).getAll();
        verify(webhookConnectionService).getAll();
    }

    @Test
    @DisplayName("getAllConnectionsV2 returns empty when no connections or webhooks")
    void getAllConnectionsV2_returnsEmpty() {
        when(activeConnectionService.getAll()).thenReturn(List.of());
        when(webhookConnectionService.getAll()).thenReturn(List.of());

        ConnectionModel.AllConnections result = connectionDetailsService.getAllConnectionsV2();

        assertThat(result).isNotNull();
        verify(activeConnectionService).getAll();
        verify(webhookConnectionService).getAll();
    }

    @Test
    @DisplayName("getAllConnectionsV2 throws when activeConnectionService fails")
    void getAllConnectionsV2_activeConnectionServiceThrows() {
        when(activeConnectionService.getAll()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> connectionDetailsService.getAllConnectionsV2());
        verify(activeConnectionService).getAll();
        verify(webhookConnectionService, never()).getAll();
    }

    @Test
    @DisplayName("updateConnectionDetails updates details when URL is valid")
    void updateConnectionDetails_validUrl_updatesDetails() {
        var dto = new ConnectionDetailsDtoJava(
                2L,
                3L,
                ConnectionType.PRIMARY,
                "edge-1",
                "https://www.valid.com",
                "eu-west");

        var connectionId = 1L;
        var detailId = 10L;
        var accountId = 100L;

        when(activeConnectionService.getAccountIdByConnectionId(connectionId)).thenReturn(accountId);
        when(connectionDetailsCacheRepository.save(anyLong(), any(ConnectionDetailsEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        var result = connectionDetailsService.updateConnectionDetails(dto, connectionId, detailId);

        assertThat(result).isNotNull();
        verify(activeConnectionService).getAccountIdByConnectionId(connectionId);
        verify(connectionDetailsCacheRepository).save(anyLong(), any(ConnectionDetailsEntity.class));
    }

    @Test
    @DisplayName("updateConnectionDetails throws when URL is invalid")
    void updateConnectionDetails_invalidUrl_throws() {
        var dto = new ConnectionDetailsDtoJava(1L,2,ConnectionType.PRIMARY,"location", "invalid-url", "region");

        assertThrows(ApplicationEsperantoException.class,
                () -> connectionDetailsService.updateConnectionDetails(dto, 1L, 10L));
        verifyNoInteractions(activeConnectionService, connectionDetailsRepository);
    }

    @Test
    @DisplayName("updateConnectionDetails throws when URL is null or empty")
    void updateConnectionDetails_nullOrEmptyUrl_throws() {
        var dto = new ConnectionDetailsDtoJava(
                1L, // id
                2,  // connectionType (or use ConnectionType.PRIMARY.getValue())
                ConnectionType.PRIMARY,
                "location",
                "invalid-url",
                "region"
        );

        assertThrows(ApplicationEsperantoException.class,
                () -> connectionDetailsService.updateConnectionDetails(dto, 1L, 10L));
        verifyNoInteractions(activeConnectionService, connectionDetailsRepository);
    }

}
