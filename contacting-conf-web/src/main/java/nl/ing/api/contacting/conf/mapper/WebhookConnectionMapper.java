package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import com.ing.api.contacting.dto.java.resource.connection.WebhookDto;
import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.WebhookConnectionEntity;
import nl.ing.api.contacting.conf.domain.model.connection.WebhookConnectionVO;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class WebhookConnectionMapper {

    /**
     * Maps a WebhookConnectionEntity to a WebhookConnectionVO domain object.
     *
     * @param entity the WebhookConnectionEntity to map
     * @return the mapped WebhookConnectionVO
     */
    public static WebhookConnectionVO toVO(WebhookConnectionEntity entity) {
        return new WebhookConnectionVO(
                entity.getId(),
                ConnectionType.fromValue(entity.getConnectionType()),
                entity.getUrl(),
                entity.getAccountId(),
                entity.isActive()
        );
    }

    /**
     * Converts a {@link WebhookDto} to a {@link WebhookConnectionEntity}.
     *
     * @param dto The DTO to convert.
     * @return The corresponding entity.
     */
    public static WebhookConnectionEntity toEntity(WebhookDto dto) {
        WebhookConnectionEntity entity = new WebhookConnectionEntity();
        entity.setId(dto.id() == null ? -1L : dto.id());
        entity.setConnectionType(dto.connectionType().getValue()); // Assuming it's stored as a String
        entity.setUrl(dto.url());
        entity.setAccountId(dto.accountId());
        entity.setActive(dto.isActive() != null && dto.isActive() != 0);
        return entity;
    }

    /**
     * Converts a {@link WebhookConnectionEntity} to a {@link WebhookDto}.
     *
     * @param entity The entity to convert.
     * @return The corresponding DTO.
     */
    public static WebhookDto toDto(WebhookConnectionEntity entity) {
        return new WebhookDto(
                entity.getId(),
                ConnectionType.fromValue(entity.getConnectionType()),
                entity.getUrl(),
                entity.getAccountId(),
                entity.isActive() ? 1 : 0
        );
    }
}
