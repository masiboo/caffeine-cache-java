package nl.ing.api.contacting.conf.domain.model.connection;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;

public record WebhookConnectionVO(Long id, ConnectionType connectionType, String url, Long accountId, Boolean active) {
}
