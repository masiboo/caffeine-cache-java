package nl.ing.api.contacting.conf.domain.model.connection;

import com.ing.api.contacting.dto.java.resource.connection.Layer;

public record ConnectionVO(Long id, Long accountId, Layer layer, String domain) {
}
