package nl.ing.api.contacting.conf.domain.model.connection;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionModel;
import com.ing.api.contacting.dto.java.resource.connection.Layer;

public record DomainLayer(Layer layer, ConnectionModel.TwilioDomain twilioDomain) {
}