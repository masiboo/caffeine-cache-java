package nl.ing.api.contacting.conf.resource.dto;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import com.ing.api.contacting.dto.java.resource.connection.Layer;


import java.util.List;
import java.util.Optional;

public class ConnectionModelV1 {

    public record UrlDetails(
            Optional<Long> id,
            String edgeLocation,
            String url
    ) {}

    public record WebhookUrlDetails(
            Long id,
            String url
    ) {}

    public record URLs(
            UrlDetails primary,
            Optional<UrlDetails> fallback,
            Optional<UrlDetails> workFromHome
    ) {}

    public record WebhookURLs(
            WebhookUrlDetails primary,
            Optional<WebhookUrlDetails> fallback
    ) {}

    public record TwilioDomain(
            Optional<Long> id,
            String domain,
            ConnectionType active,
            URLs urls
    ) {}

    public record Webhook(
            ConnectionType active,
            WebhookURLs urls
    ) {}

    public record Connection(
            long accountId,
            List<TwilioDomain> frontend,
            List<TwilioDomain> backend,
            Optional<Webhook> webhooks
    ) {}

    public record BackendConnections(
            List<TwilioDomain> backend
    ) {}

    public record FrontendConnections(
            List<TwilioDomain> frontend
    ) {}

    public record AllConnections(
            List<Connection> connections
    ) {}

    public record DomainLayerV1(
            Layer layer,
            TwilioDomain twilioDomain
    ) {}
}