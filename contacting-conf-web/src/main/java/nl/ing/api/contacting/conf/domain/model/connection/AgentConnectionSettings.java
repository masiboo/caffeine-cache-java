package nl.ing.api.contacting.conf.domain.model.connection;

import nl.ing.api.contacting.conf.resource.dto.ConnectionModelV1;

public record AgentConnectionSettings(
        Boolean wfhIndicator,
        ConnectionModelV1.FrontendConnections frontendConnections
) {
}
