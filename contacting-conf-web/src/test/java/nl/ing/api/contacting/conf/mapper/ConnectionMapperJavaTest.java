package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionDetailsDtoJava;
import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import com.ing.api.contacting.dto.java.resource.connection.Layer;
import com.ing.api.contacting.dto.java.resource.connection.WebhookDto;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsVO;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionVO;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionWithDetails;
import nl.ing.api.contacting.conf.domain.model.connection.WebhookConnectionVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConnectionMapperJavaTest")
class ConnectionMapperJavaTest {

    @Nested
    @DisplayName("toDTOV1")
    class ToDTOV1 {

        @Test
        @DisplayName("should return empty list when input is empty")
        void shouldReturnEmptyListWhenInputIsEmpty() {
            var result = ConnectionMapperJava.toDTOV1(List.of(), List.of());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should map connections and webhooks correctly")
        void shouldMapConnectionsAndWebhooksCorrectly() {
            var connection = TestFixtures.connectionWithDetails(1L, Layer.FRONTEND, ConnectionType.PRIMARY, true);
            var webhook = TestFixtures.webhookConnectionVO(1L, ConnectionType.PRIMARY, true, 1L);

            var result = ConnectionMapperJava.toDTOV1(List.of(connection), List.of(webhook));
            assertThat(result).hasSize(1);
            assertThat(result.get(0).accountId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("toDTO (accountId overload)")
    class ToDTOWithAccountId {

        @Test
        @DisplayName("should return connection for given accountId")
        void shouldReturnConnectionForGivenAccountId() {
            var connection = TestFixtures.connectionWithDetails(2L, Layer.BACKEND, ConnectionType.PRIMARY, true);
            var webhook = TestFixtures.webhookConnectionVO(2L, ConnectionType.PRIMARY, true, 2L);

            var result = ConnectionMapperJava.toDTO(List.of(connection), List.of(webhook), 2L);
            assertThat(result.accountId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("should return empty domains if accountId not found")
        void shouldReturnEmptyDomainsIfAccountIdNotFound() {
            var result = ConnectionMapperJava.toDTO(List.of(), List.of(), 99L);
            assertThat(result.frontend()).isEmpty();
            assertThat(result.backend()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toDTO (list overload)")
    class ToDTOList {

        @Test
        @DisplayName("should group by account and map webhooks")
        void shouldGroupByAccountAndMapWebhooks() {
            var connection1 = TestFixtures.connectionWithDetails(1L, Layer.FRONTEND, ConnectionType.PRIMARY, true);
            var connection2 = TestFixtures.connectionWithDetails(2L, Layer.BACKEND, ConnectionType.PRIMARY, true);
            var webhook1 = TestFixtures.webhookConnectionVO(1L, ConnectionType.PRIMARY, true, 1L);
            var webhook2 = TestFixtures.webhookConnectionVO(2L, ConnectionType.PRIMARY, true, 2L);

            var result = ConnectionMapperJava.toDTO(List.of(connection1, connection2), List.of(webhook1, webhook2));
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("webhookDTO")
    class WebhookDTO {

        @Test
        @DisplayName("should return empty when no primary webhook")
        void shouldReturnEmptyWhenNoPrimaryWebhook() {
            var webhook = TestFixtures.webhookConnectionVO(1L, ConnectionType.FALLBACK, true, 1L);
            var result = ConnectionMapperJava.webhookDTO(List.of(webhook));
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should map primary and fallback webhooks")
        void shouldMapPrimaryAndFallbackWebhooks() {
            var primary = TestFixtures.webhookConnectionVO(1L, ConnectionType.PRIMARY, true, 1L);
            var fallback = TestFixtures.webhookConnectionVO(2L, ConnectionType.FALLBACK, true, 1L);

            var result = ConnectionMapperJava.webhookDTO(List.of(primary, fallback));
            assertThat(result).isPresent();
            assertThat(result.get().active()).isEqualTo(ConnectionType.PRIMARY);
            assertThat(result.get().urls().primary().url()).isEqualTo(primary.url());
            assertThat(result.get().urls().fallback()).isPresent();
        }
    }

    @Nested
    @DisplayName("webhookConnectionsToEntities")
    class WebhookConnectionsToEntities {

        @Test
        @DisplayName("should map VO to entity")
        void shouldMapVOToEntity() {
            var vo = TestFixtures.webhookConnectionVO(1L, ConnectionType.PRIMARY, true, 1L);
            var result = ConnectionMapperJava.webhookConnectionsToEntities(List.of(vo));
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("toVO (ConnectionDetailsDtoJava)")
    class ToVOConnectionDetailsDtoJava {

        @Test
        @DisplayName("should map DTO to VO")
        void shouldMapDTOToVO() {
            var dto = TestFixtures.connectionDetailsDtoJava(1L, 2L, ConnectionType.PRIMARY, "edge", "url", "region");
            ConnectionDetailsVO vo = ConnectionMapperJava.toVO(dto);

            assertThat(vo.id()).isEqualTo(1L);
            assertThat(vo.connectionId()).isEqualTo(2L);
            assertThat(vo.connectionType()).isEqualTo(ConnectionType.PRIMARY);
            assertThat(vo.edgeLocation()).isEqualTo("edge");
            assertThat(vo.url()).isEqualTo("url");
            assertThat(vo.region()).isEqualTo("region");
        }

    }

    @Nested
    @DisplayName("toVO (WebhookDto)")
    class ToVOWebhookDto {

        @Test
        @DisplayName("should map WebhookDto to VO")
        void shouldMapWebhookDtoToVO() {
            var dto = TestFixtures.webhookDto(1L, ConnectionType.PRIMARY, "url", 1L, 1);
            var result = ConnectionMapperJava.toVO(List.of(dto));
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("toDomainDTO")
    class ToDomainDTO {

        @Test
        @DisplayName("should return domains for given layer")
        void shouldReturnDomainsForGivenLayer() {
            var connection = TestFixtures.connectionWithDetails(1L, Layer.FRONTEND, ConnectionType.PRIMARY, true);
            var result = ConnectionMapperJava.toDomainDTO(List.of(connection), Layer.FRONTEND);
            assertThat(result).isNotEmpty();
        }
    }

    // TestFixtures is a static utility class for creating test data
    static class TestFixtures {
        static ConnectionWithDetails connectionWithDetails(Long accountId, Layer layer, ConnectionType type, boolean active) {
            var vo = new ConnectionVO(1L, accountId, layer, "domain");
            var detailsVO = new ConnectionDetailsVO(1L, 1L, type, "edge", "url", "region");
            return new ConnectionWithDetails(vo, detailsVO, active);
        }

        static WebhookConnectionVO webhookConnectionVO(Long id, ConnectionType type, boolean active, Long accountId) {
            return new WebhookConnectionVO(id, type, "url", accountId, active);
        }

        static ConnectionDetailsDtoJava connectionDetailsDtoJava(Long id, Long connectionId, ConnectionType type, String edge, String url, String region) {
            return new ConnectionDetailsDtoJava(id, connectionId, type, edge, url, region);
        }

        static WebhookDto webhookDto(Long id, ConnectionType type, String url, Long accountId, int isActive) {
            return new WebhookDto(id, type, url, accountId, isActive);
        }
    }
}