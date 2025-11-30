package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionVO;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionWithDetails;
import com.ing.api.contacting.dto.java.resource.connection.Layer;
import nl.ing.api.contacting.conf.domain.model.connection.WebhookConnectionVO;
import nl.ing.api.contacting.conf.resource.dto.ConnectionModelV1;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for mapping connection domain objects to DTOs for API responses.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ConnectionMapperJavaV1 {

    /**
     * Maps a list of {@link ConnectionWithDetails} and {@link WebhookConnectionVO} to a list of {@link ConnectionModelV1.Connection}.
     */
    public static List<ConnectionModelV1.Connection> toDTOV1(
            List<ConnectionWithDetails> connections,
            List<WebhookConnectionVO> webhooks) {

        Map<Long, List<ConnectionModelV1.DomainLayerV1>> grouped = groupedByAccount(connections);

        return grouped.entrySet().stream()
                .map(entry -> buildConnection(entry.getKey(), entry.getValue(), webhooks))
                .toList();
    }

    /**
     * Maps connection details and webhooks to a single API DTO connection for a given context.
     */
    public static ConnectionModelV1.Connection toDTO(
            List<ConnectionWithDetails> connections,
            List<WebhookConnectionVO> webhooks,
            Long accountId) {

        Map<Long, List<ConnectionModelV1.DomainLayerV1>> grouped = groupedByAccount(connections);
        List<ConnectionModelV1.DomainLayerV1> domainLayers = grouped.getOrDefault(accountId, Collections.emptyList());

        return buildConnection(accountId, domainLayers, webhooks);
    }

    /**
     * Builds a {@link ConnectionModelV1.Connection} for a specific account.
     */
    private static ConnectionModelV1.Connection buildConnection(
            Long accountId,
            List<ConnectionModelV1.DomainLayerV1> domainLayers,
            List<WebhookConnectionVO> allWebhooks) {

        List<WebhookConnectionVO> accountWebhooks = filterWebhooksByAccount(allWebhooks, accountId);
        Optional<ConnectionModelV1.Webhook> webhook = mapWebhookDTO(accountWebhooks);

        List<ConnectionModelV1.TwilioDomain> frontendDomains = getDomainUrlByLayer(domainLayers, Layer.FRONTEND);
        List<ConnectionModelV1.TwilioDomain> backendDomains = getDomainUrlByLayer(domainLayers, Layer.BACKEND);

        return new ConnectionModelV1.Connection(accountId, frontendDomains, backendDomains, webhook);
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
    private static Optional<ConnectionModelV1.Webhook> mapWebhookDTO(List<WebhookConnectionVO> webhooks) {
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
        ConnectionType active = primary.active() ? ConnectionType.PRIMARY : ConnectionType.FALLBACK;

        Optional<ConnectionModelV1.WebhookUrlDetails> fallbackOpt = webhooks.stream()
                .filter(w -> ConnectionType.FALLBACK.equals(w.connectionType()))
                .findFirst()
                .map(fb -> new ConnectionModelV1.WebhookUrlDetails(fb.id(), fb.url()));

        ConnectionModelV1.WebhookUrlDetails primaryDetails =
                new ConnectionModelV1.WebhookUrlDetails(primary.id(), primary.url());

        ConnectionModelV1.WebhookURLs webhookURLs = new ConnectionModelV1.WebhookURLs(primaryDetails, fallbackOpt);

        return Optional.of(new ConnectionModelV1.Webhook(active, webhookURLs));
    }

    /**
     * Groups connection details by account for API DTO mapping.
     */
    private static Map<Long, List<ConnectionModelV1.DomainLayerV1>> groupedByAccount(
            List<ConnectionWithDetails> connections) {

        if (connections == null || connections.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<ConnectionVO, List<ConnectionWithDetails>> groupedByConnection =
                connections.stream().collect(Collectors.groupingBy(ConnectionWithDetails::connectionVO));

        Map<Long, List<ConnectionModelV1.DomainLayerV1>> result = new HashMap<>();

        groupedByConnection.forEach((connection, detailsList) -> {
            Optional<ConnectionModelV1.TwilioDomain> twilioDomain = createTwilioDomain(connection, detailsList);

            twilioDomain.ifPresent(domain ->
                    result.computeIfAbsent(connection.accountId(), k -> new ArrayList<>())
                            .add(new ConnectionModelV1.DomainLayerV1(connection.layer(), domain))
            );
        });

        return result;
    }

    /**
     * Creates a TwilioDomain from connection details.
     */
    private static Optional<ConnectionModelV1.TwilioDomain> createTwilioDomain(
            ConnectionVO connection, List<ConnectionWithDetails> detailsList) {

        Optional<ConnectionModelV1.UrlDetails> primary = getURLs(detailsList, ConnectionType.PRIMARY);
        if (primary.isEmpty()) {
            return Optional.empty();
        }

        Optional<ConnectionModelV1.UrlDetails> fallback = getURLs(detailsList, ConnectionType.FALLBACK);
        Optional<ConnectionModelV1.UrlDetails> workFromHome = getURLs(detailsList, ConnectionType.WORK_FROM_HOME);

        ConnectionType active = detailsList.stream()
                .filter(ConnectionWithDetails::active)
                .map(c -> c.connectionDetailsVO().connectionType())
                .findFirst()
                .orElse(ConnectionType.PRIMARY);

        ConnectionModelV1.URLs urls = new ConnectionModelV1.URLs(
                primary.get(), fallback, workFromHome
        );

        return Optional.of(new ConnectionModelV1.TwilioDomain(
                Optional.of(connection.id()),
                connection.domain(),
                active,
                urls
        ));
    }

    /**
     * Gets the first URL details for a given connection type.
     */
    private static Optional<ConnectionModelV1.UrlDetails> getURLs(
            List<ConnectionWithDetails> details, ConnectionType type) {

        if (details == null || details.isEmpty()) {
            return Optional.empty();
        }

        return details.stream()
                .filter(c -> c.connectionDetailsVO().connectionType() == type)
                .findFirst()
                .map(c -> new ConnectionModelV1.UrlDetails(
                        Optional.of(c.connectionDetailsVO().id()),
                        c.connectionDetailsVO().edgeLocation(),
                        c.connectionDetailsVO().url()
                ));
    }

    /**
     * Filters domain layers by layer type for API DTO mapping.
     */
    private static List<ConnectionModelV1.TwilioDomain> getDomainUrlByLayer(
            List<ConnectionModelV1.DomainLayerV1> domainLayers, Layer layer) {

        if (domainLayers == null || domainLayers.isEmpty()) {
            return Collections.emptyList();
        }

        return domainLayers.stream()
                .filter(dl -> dl.layer() == layer)
                .map(ConnectionModelV1.DomainLayerV1::twilioDomain)
                .toList();
    }

    /**
     * Maps a list of {@link ConnectionWithDetails} to a list of {@link ConnectionModelV1.TwilioDomain} for a specific layer.
     */
    public static List<ConnectionModelV1.TwilioDomain> toDomainDTO(List<ConnectionWithDetails> connections, Layer layer) {
        return groupedByAccount(connections).values().stream()
                .flatMap(domainLayers -> getDomainUrlByLayer(domainLayers, layer).stream())
                .toList();
    }
}
