package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import com.ing.api.contacting.dto.java.resource.connection.Layer;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.ConnectionDetailsEntity;
import nl.ing.api.contacting.conf.domain.entity.ConnectionEntity;
import nl.ing.api.contacting.conf.domain.model.connection.*;

import java.util.List;

@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ConnectionDetailsMapper {

    public static ConnectionVO toVO(ConnectionEntity entity) {
        return new ConnectionVO(
                entity.getId(),
                entity.getAccountId(),
                Layer.fromValue(entity.getLayer()),
                entity.getDomain());
    }

    public static ConnectionDetailsVO toConnectionDetailsVO(ConnectionDetailsEntity entity) {
        return new ConnectionDetailsVO(
               entity.getId(),
                entity.getConnection().getAccountId(),
                ConnectionType.fromValue(entity.getConnectionType()),
                entity.getEdgeLocation(),
                entity.getUrl(),
                entity.getRegion());
    }

    public static ConnectionDetailsVO withDetails(ConnectionDetailsVO entity, Long detailId, Long connectionId) {
        return new ConnectionDetailsVO(
                detailId,
                connectionId,
                entity.connectionType(),
                entity.edgeLocation(),
                entity.url(),
                entity.region());
    }

    public static ConnectionDetailsEntity toEntity(ConnectionDetailsVO vo) {
        return new ConnectionDetailsEntity(
                vo.id(),
                ConnectionEntity.builder().id(vo.connectionId()).build(),
                vo.connectionType().getValue(),
                vo.edgeLocation(),
                vo.url(),
                vo.region());
    }

    public static List<ConnectionWithDetails> toDto(List<ConnectionDetailsDTO> details) {
        return details.stream()
                .map(detail -> new ConnectionWithDetails(
                        toVO(detail.connection()),
                        toConnectionDetailsVO(detail.details()),
                        detail.active().getConnectionDetails().getId().equals(detail.connection().getId())
                ))
                .toList();
    }
}
