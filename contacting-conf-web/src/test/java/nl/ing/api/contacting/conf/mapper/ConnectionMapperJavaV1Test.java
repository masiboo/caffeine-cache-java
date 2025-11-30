package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import com.ing.api.contacting.dto.java.resource.connection.Layer;
import nl.ing.api.contacting.conf.domain.model.connection.*;
import nl.ing.api.contacting.conf.helper.ConnectionTestData;
import nl.ing.api.contacting.conf.resource.dto.ConnectionModelV1;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionMapperJavaV1Test {

    private ConnectionVO createConnectionVO(Long id, Long accountId, String domain, Layer layer) {
        return new ConnectionVO(id, accountId, layer, domain);
    }

    private ConnectionDetailsVO createConnectionDetailsVO(Long id, Long connectionId, ConnectionType connectionType, String edgeLocation, String url, String region) {
        return new ConnectionDetailsVO(id, connectionId, connectionType, edgeLocation, url, region);
    }

    private WebhookConnectionVO createWebhookConnectionVO(Long id, Long accountId, String url, ConnectionType connectionType, Boolean active) {
        return new WebhookConnectionVO(id, connectionType, url, accountId, active);
    }

    private ConnectionWithDetails createConnectionWithDetails(ConnectionVO connectionVO, ConnectionDetailsVO connectionDetailsVO, boolean active) {
        return new ConnectionWithDetails(connectionVO, connectionDetailsVO, active);
    }

    @Test
    @DisplayName("toDTO maps ConnectionWithDetails and WebhookConnectionVO to ConnectionModelV1.Connection")
    void toDTO_mapsCorrectly() {
        Long accountId = 1001L;

        // Prepare test data using test data helper
        ConnectionWithDetails details = ConnectionTestData.getConnectionWithDetails();
        WebhookConnectionVO webhook = ConnectionTestData.getWebhookConnectionVO();

        List<ConnectionWithDetails> connectionList = List.of(details);
        List<WebhookConnectionVO> webhookList = List.of(webhook);

        ConnectionModelV1.Connection result = ConnectionMapperJavaV1.toDTO(connectionList, webhookList, accountId);

        assertEquals(accountId, result.accountId());
        assertTrue(result.backend().isEmpty());
        assertTrue(result.frontend().isEmpty());
        assertFalse(result.webhooks().isPresent());
    }

    @Test
    @DisplayName("toDTOV1 maps multiple accounts and webhooks")
    void toDTOV1_mapsMultipleAccounts() {
        Long accountId1 = 1L;
        Long accountId2 = 2L;

        ConnectionVO vo1 = createConnectionVO(1L, accountId1, "domainName", Layer.BACKEND);
        ConnectionVO vo2 = createConnectionVO(2L, accountId2, "domainName", Layer.FRONTEND);

        ConnectionWithDetails details1 = createConnectionWithDetails(vo1, createConnectionDetailsVO(101L, 1L, ConnectionType.PRIMARY, "Edge1", "https://1", "region1"), true);
        ConnectionWithDetails details2 = createConnectionWithDetails(vo2, createConnectionDetailsVO(102L, 2L, ConnectionType.PRIMARY, "Edge2", "https://2", "region2"), true);

        WebhookConnectionVO webhook1 = createWebhookConnectionVO(11L, accountId1, "https://wh1", ConnectionType.PRIMARY, true);
        WebhookConnectionVO webhook2 = createWebhookConnectionVO(12L, accountId2, "https://wh2", ConnectionType.PRIMARY, true);

        List<ConnectionWithDetails> detailsList = List.of(details1, details2);
        List<WebhookConnectionVO> webhookList = List.of(webhook1, webhook2);

        List<ConnectionModelV1.Connection> result = ConnectionMapperJavaV1.toDTOV1(detailsList, webhookList);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c ->Objects.equals(c.accountId(),accountId1)));
        assertTrue(result.stream().anyMatch(c ->Objects.equals(c.accountId(),accountId2)));
    }

    @Test
    @DisplayName("toDomainDTO returns TwilioDomain for given layer")
    void toDomainDTO_returnsDomainsForLayer() {
        Long accountId = 99L;

        ConnectionVO backendVO = createConnectionVO(1L, accountId, "backend", Layer.BACKEND);
        ConnectionVO frontendVO = createConnectionVO(2L, accountId, "frontend", Layer.FRONTEND);

        ConnectionWithDetails backendDetails = createConnectionWithDetails(backendVO, createConnectionDetailsVO(101L, 1L, ConnectionType.PRIMARY, "EdgeB", "https://b", "regionB"), true);
        ConnectionWithDetails frontendDetails = createConnectionWithDetails(frontendVO, createConnectionDetailsVO(102L, 2L, ConnectionType.PRIMARY, "EdgeF", "https://f", "regionF"), true);

        List<ConnectionWithDetails> allDetails = List.of(backendDetails, frontendDetails);

        List<ConnectionModelV1.TwilioDomain> backendDomains = ConnectionMapperJavaV1.toDomainDTO(allDetails, Layer.BACKEND);
        List<ConnectionModelV1.TwilioDomain> frontendDomains = ConnectionMapperJavaV1.toDomainDTO(allDetails, Layer.FRONTEND);

        assertEquals(1, backendDomains.size());
        assertEquals("backend", backendDomains.get(0).domain());

        assertEquals(1, frontendDomains.size());
        assertEquals("frontend", frontendDomains.get(0).domain());
    }
}
