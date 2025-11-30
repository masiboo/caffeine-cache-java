package nl.ing.api.contacting.conf.domain.model.connection;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;

public record ConnectionDetailsVO(Long id, Long connectionId, ConnectionType connectionType, String edgeLocation,
                                  String url, String region) {
}
