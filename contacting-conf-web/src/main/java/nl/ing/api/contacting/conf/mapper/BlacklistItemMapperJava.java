package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistFunctionality;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistItemDto;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistType;
import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.BlacklistEntity;
import nl.ing.api.contacting.conf.domain.model.blacklist.BlacklistItemVO;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class BlacklistItemMapperJava {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // VO → DTO
    public static BlacklistItemDto toDto(BlacklistItemVO vo) {
        return new BlacklistItemDto(
                Optional.ofNullable(vo.id()),
                vo.functionality(),
                vo.entityType(),
                vo.value(),
                vo.startDate().format(FORMATTER),
                vo.endDate().map(d -> d.format(FORMATTER))
        );
    }

    // DTO → VO (without accountId)
    public static BlacklistItemVO fromDto(BlacklistItemDto dto) {
        return new BlacklistItemVO(
                dto.id().orElse(null),
                dto.functionality(),
                dto.entityType(),
                HtmlUtils.htmlEscape(dto.value()),
                LocalDateTime.parse(dto.startDate(), FORMATTER),
                dto.endDate().map(d -> LocalDateTime.parse(d, FORMATTER))
        );
    }

    public static BlacklistItemVO toVO(BlacklistEntity entity) {
        if (entity == null) return null;

        return new BlacklistItemVO(
                entity.getId(),
                BlacklistFunctionality.valueOf(entity.getFunctionality()),
                BlacklistType.valueOf(entity.getEntityType()),
                entity.getValue(),
                entity.getStartDate(),
                Optional.ofNullable(entity.getEndDate())
        );
    }

    // VO → Entity (with accountId)
    public static BlacklistEntity toEntity(BlacklistItemVO vo, Long accountId) {
        if (vo == null) return null;
        BlacklistEntity entity = new BlacklistEntity();
        entity.setId(vo.id());
        entity.setFunctionality(vo.functionality().value());
        entity.setEntityType(vo.entityType().value());
        entity.setValue(vo.value());
        entity.setStartDate(vo.startDate());
        entity.setEndDate(vo.endDate().orElse(null));
        entity.setAccountId(accountId);
        return entity;
    }
}
