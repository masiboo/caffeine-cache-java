package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.connection.*;
import com.ing.api.contacting.dto.java.resource.connection.Layer;
import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.WebhookConnectionEntity;
import nl.ing.api.contacting.conf.domain.model.connection.*;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsVO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for mapping connection domain objects to DTOs for API responses.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ConnectionMapperJava {

    public static List<ConnectionModel.Connection> toDTOV1(
            List<ConnectionWithDetails> connections,
            List<WebhookConnectionVO> webhooks) {

        Map<Long, List<DomainLayer>> grouped = groupedByAccount(connections);

        return grouped.entrySet().stream()
                .map(entry -> buildConnection(entry.getKey(), entry.getValue(), webhooks))
                .toList();
    }

    /**
     * Maps connection details and webhooks to a single API DTO connection for a given context.
     */
    public static ConnectionModel.Connection toDTO(
            List<ConnectionWithDetails> connections,
            List<WebhookConnectionVO> webhooks,
            Long accountId) {

        Map<Long, List<DomainLayer>> grouped = groupedByAccount(connections);
        List<DomainLayer> domainLayers = grouped.getOrDefault(accountId, Collections.emptyList());

        return buildConnection(accountId, domainLayers, webhooks);
    }


    public static List<ConnectionModel.Connection> toDTO(List<ConnectionWithDetails> connectionVOs, List<WebhookConnectionVO> webhooks) {
        Map<Long, List<DomainLayer>> grouped = groupedByAccount(connectionVOs);
        List<ConnectionModel.Connection> result = new ArrayList<>();
        for (Map.Entry<Long, List<DomainLayer>> entry : grouped.entrySet()) {
            Long accountId = entry.getKey();
            List<WebhookConnectionVO> accountWebhooks = webhooks.stream()
                    .filter(w -> Objects.equals(w.accountId(), accountId))
                    .toList();
            Optional<ConnectionModel.Webhook> webhook = webhookDTO(accountWebhooks);
            List<ConnectionModel.TwilioDomain> fe = getDomainUrlByLayer(entry.getValue(), Layer.FRONTEND);
            List<ConnectionModel.TwilioDomain> be = getDomainUrlByLayer(entry.getValue(), Layer.BACKEND);
            result.add(new ConnectionModel.Connection(accountId, fe, be, webhook));
        }
        return result;
    }

    public static Optional<ConnectionModel.Webhook> webhookDTO(List<WebhookConnectionVO> webhooks) {
        return webhooks.stream()
                .filter(w -> w.connectionType() == ConnectionType.PRIMARY)
                .findFirst()
                .map(pm -> {
                    // Use primitive boolean directly
                    ConnectionType active = pm.active() ? ConnectionType.PRIMARY : ConnectionType.FALLBACK;
                    Optional<ConnectionModel.WebhookUrlDetails> fallback = webhooks.stream()
                            .filter(w -> w.connectionType() == ConnectionType.FALLBACK)
                            .findFirst()
                            .map(fb -> new ConnectionModel.WebhookUrlDetails(Optional.of(fb.id()), fb.url()));
                    ConnectionModel.WebhookUrlDetails primary = new ConnectionModel.WebhookUrlDetails(Optional.of(pm.id()), pm.url());
                    return new ConnectionModel.Webhook(active, new ConnectionModel.WebhookURLs(primary, fallback));
                });
    }

    public static List<WebhookConnectionEntity> webhookConnectionsToEntities(List<WebhookConnectionVO> webhookConnections) {
        return webhookConnections.stream()
                .map(conn -> new WebhookConnectionEntity(
                        conn.id(),
                        conn.connectionType().getValue(),
                        conn.url(),
                        conn.accountId(),
                        conn.active()))
                .toList();
    }

    public static ConnectionDetailsVO toVO(ConnectionDetailsDtoJava connectionDetailsDto) {
        return new ConnectionDetailsVO(
                connectionDetailsDto.id(),
                connectionDetailsDto.connectionId(),
                connectionDetailsDto.connectionType(),
                connectionDetailsDto.edgeLocation(),
                connectionDetailsDto.url(),
                connectionDetailsDto.region());
    }

    public static List<WebhookConnectionVO> toVO(List<WebhookDto> dtos) {
        return dtos.stream()
                .map(dto -> new WebhookConnectionVO(dto.id(), dto.connectionType(), dto.url(), dto.accountId(), dto.isActive() == 1))
                .toList();
    }

    private static ConnectionModel.Connection buildConnection(
            Long accountId,
            List<DomainLayer> domainLayers,
            List<WebhookConnectionVO> allWebhooks) {

        List<WebhookConnectionVO> accountWebhooks = filterWebhooksByAccount(allWebhooks, accountId);
        Optional<ConnectionModel.Webhook> webhook = mapWebhookDTO(accountWebhooks);

        List<ConnectionModel.TwilioDomain> frontendDomains = getDomainUrlByLayer(domainLayers, Layer.FRONTEND);
        List<ConnectionModel.TwilioDomain> backendDomains = getDomainUrlByLayer(domainLayers, Layer.BACKEND);

        return new ConnectionModel.Connection(accountId, frontendDomains, backendDomains, webhook);
    }

    /**
     * Filters webhooks by account ID.
     */
    private static List<WebhookConnectionVO> filterWebhooksByAccount(List<WebhookConnectionVO> webhooks, Long accountId) {
        return Optional.ofNullable(webhooks)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(w -> Objects.equals(w.accountId(), accountId))
                .toList();
    }

    /**
     * Maps a list of webhook connections to an optional API webhook DTO.
     */
    private static Optional<ConnectionModel.Webhook> mapWebhookDTO(List<WebhookConnectionVO> webhooks) {
        if (webhooks == null || webhooks.isEmpty()) {
            return Optional.empty();
        }

        Optional<WebhookConnectionVO> primaryOpt = webhooks.stream()
                .filter(w -> w.connectionType() == ConnectionType.PRIMARY)
                .findFirst();

        if (primaryOpt.isEmpty()) {
            return Optional.empty();
        }

        WebhookConnectionVO primary = primaryOpt.get();
        // Use primitive boolean directly
        ConnectionType active = primary.active() ? ConnectionType.PRIMARY : ConnectionType.FALLBACK;

        Optional<ConnectionModel.WebhookUrlDetails> fallbackOpt = webhooks.stream()
                .filter(w -> ConnectionType.FALLBACK.equals(w.connectionType()))
                .findFirst()
                .map(fb -> new ConnectionModel.WebhookUrlDetails(Optional.of(fb.id()), fb.url()));

        ConnectionModel.WebhookUrlDetails primaryDetails =
                new ConnectionModel.WebhookUrlDetails(Optional.of(primary.id()), primary.url());

        ConnectionModel.WebhookURLs webhookURLs = new ConnectionModel.WebhookURLs(primaryDetails, fallbackOpt);

        return Optional.of(new ConnectionModel.Webhook(active, webhookURLs));
    }

    /**
     * Groups connection details by account for API DTO mapping.
     */
    private static Map<Long, List<DomainLayer>> groupedByAccount(
            List<ConnectionWithDetails> connections) {

        if (connections == null) {
            return Collections.emptyMap();
        }

        Map<ConnectionVO, List<ConnectionWithDetails>> groupedByConnection =
                connections.stream().collect(Collectors.groupingBy(ConnectionWithDetails::connectionVO));

        Map<Long, List<DomainLayer>> result = new HashMap<>();

        groupedByConnection.forEach((connection, detailsList) ->
            createTwilioDomain(connection, detailsList).ifPresent(domain ->
                result.computeIfAbsent(connection.accountId(), k -> new ArrayList<>())
                        .add(new DomainLayer(connection.layer(), domain))
            )
        );

        return result;
    }

    /**
     * Creates a TwilioDomain from connection details.
     */
    private static Optional<ConnectionModel.TwilioDomain> createTwilioDomain(
            ConnectionVO connection, List<ConnectionWithDetails> detailsList) {

        Optional<ConnectionModel.UrlDetails> primary = getURLs(detailsList, ConnectionType.PRIMARY);
        if (primary.isEmpty()) {
            return Optional.empty();
        }

        Optional<ConnectionModel.UrlDetails> fallback = getURLs(detailsList, ConnectionType.FALLBACK);
        Optional<ConnectionModel.UrlDetails> workFromHome = getURLs(detailsList, ConnectionType.WORK_FROM_HOME);

        ConnectionType active = detailsList.stream()
                .filter(ConnectionWithDetails::active)
                .map(c -> c.connectionDetailsVO().connectionType())
                .findFirst()
                .orElse(ConnectionType.PRIMARY);

        ConnectionModel.URLs urls = new ConnectionModel.URLs(
                primary.get(), fallback, workFromHome
        );

        return Optional.of(new ConnectionModel.TwilioDomain(
                Optional.of(connection.id()),
                connection.domain(),
                active,
                urls
        ));
    }

    /**
     * Gets the first URL details for a given connection type.
     */
    private static Optional<ConnectionModel.UrlDetails> getURLs(
            List<ConnectionWithDetails> details, ConnectionType type) {

        if (details == null || details.isEmpty()) {
            return Optional.empty();
        }

        return details.stream()
                .filter(c -> c.connectionDetailsVO().connectionType() == type)
                .findFirst()
                .map(c -> new ConnectionModel.UrlDetails(
                        Optional.of(c.connectionDetailsVO().id()),
                        c.connectionDetailsVO().edgeLocation(),
                        c.connectionDetailsVO().url(),
                        Optional.ofNullable(c.connectionDetailsVO().region())
                ));
    }

    /**
     * Filters domain layers by layer type for API DTO mapping.
     */
    private static List<ConnectionModel.TwilioDomain> getDomainUrlByLayer(
            List<DomainLayer> domainLayers, Layer layer) {

        if (domainLayers == null || domainLayers.isEmpty()) {
            return Collections.emptyList();
        }

        return domainLayers.stream()
                .filter(dl -> dl.layer() == layer)
                .map(DomainLayer::twilioDomain)
                .toList();
    }

    public static List<ConnectionModel.TwilioDomain> toDomainDTO(List<ConnectionWithDetails> connections, Layer layer) {
        return groupedByAccount(connections).values().stream()
                .flatMap(domainLayers -> getDomainUrlByLayer(domainLayers, layer).stream())
                .toList();
    }

}
