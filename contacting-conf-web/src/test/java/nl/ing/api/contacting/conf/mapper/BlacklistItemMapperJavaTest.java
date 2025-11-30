package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistFunctionality;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistItemDto;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistType;
import nl.ing.api.contacting.conf.domain.entity.BlacklistEntity;
import nl.ing.api.contacting.conf.domain.model.blacklist.BlacklistItemVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BlacklistItemMapperJavaTest {

    private static final LocalDateTime START = LocalDateTime.of(2025, 10, 25, 10, 00);
    private static final LocalDateTime END = LocalDateTime.of(2025, 10, 26, 10, 00);

    private BlacklistItemVO sampleVO() {
        return new BlacklistItemVO(
                1L,
                BlacklistFunctionality.SURVEY,
                BlacklistType.PHONE_NUMBER,
                "testValue",
                START,
                Optional.of(END)
        );
    }

    private BlacklistEntity sampleEntity() {
        BlacklistEntity entity = new BlacklistEntity();
        entity.setId(1L);
        entity.setFunctionality("SURVEY");
        entity.setEntityType("PHONE_NUMBER");
        entity.setValue("testValue");
        entity.setStartDate(START);
        entity.setEndDate(END);
        entity.setAccountId(100L);
        return entity;
    }

    private BlacklistItemDto sampleDto() {
        return new BlacklistItemDto(
                Optional.of(1L),
                BlacklistFunctionality.SURVEY,
                BlacklistType.PHONE_NUMBER,
                "testValue",
                START.toString(),
                Optional.of(END.toString())
        );
    }

    @Test
    void toDto_mapsAllFields() {
        BlacklistItemVO vo = sampleVO();
        BlacklistItemDto dto = BlacklistItemMapperJava.toDto(vo);

        assertThat(dto.id().get()).isEqualTo(vo.id());
        assertThat(dto.functionality()).isEqualTo(vo.functionality());
        assertThat(dto.entityType()).isEqualTo(vo.entityType());
        assertThat(dto.value()).isEqualTo(vo.value());
        assertThat(LocalDateTime.parse(dto.startDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .isEqualTo(vo.startDate());
        assertThat(dto.endDate().map(e -> LocalDateTime.parse(e, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .contains(vo.endDate().get());
    }

    @Test
    void fromDto_mapsAllFields() {
        BlacklistItemDto dto = sampleDto();
        BlacklistItemVO vo = BlacklistItemMapperJava.fromDto(dto);

        assertThat(vo.id()).isEqualTo(dto.id().orElse(null));
        assertThat(vo.functionality()).isEqualTo(dto.functionality());
        assertThat(vo.entityType()).isEqualTo(dto.entityType());
        assertThat(vo.value()).isEqualTo(dto.value());
        assertThat(LocalDateTime.parse(dto.startDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .isEqualTo(vo.startDate());
        assertThat(vo.endDate()).contains(LocalDateTime.parse(dto.endDate().get(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void toVO_mapsEntityToVO() {
        BlacklistEntity entity = sampleEntity();
        BlacklistItemVO vo = BlacklistItemMapperJava.toVO(entity);

        assertThat(vo.id()).isEqualTo(entity.getId());
        assertThat(vo.functionality()).isEqualTo(BlacklistFunctionality.valueOf(entity.getFunctionality()));
        assertThat(vo.entityType()).isEqualTo(BlacklistType.valueOf(entity.getEntityType()));
        assertThat(vo.value()).isEqualTo(entity.getValue());
        assertThat(vo.startDate()).isEqualTo(entity.getStartDate());
        assertThat(vo.endDate()).contains(entity.getEndDate());
    }

    @Test
    void toVO_returnsNullForNullEntity() {
        assertThat(BlacklistItemMapperJava.toVO(null)).isNull();
    }

    @Test
    void toEntity_mapsVOToEntity() {
        BlacklistItemVO vo = sampleVO();
        Long accountId = 100L;
        BlacklistEntity entity = BlacklistItemMapperJava.toEntity(vo, accountId);

        assertThat(entity.getId()).isEqualTo(vo.id());
        assertThat(entity.getFunctionality()).isEqualTo(vo.functionality().name());
        assertThat(entity.getEntityType()).isEqualTo(vo.entityType().name());
        assertThat(entity.getValue()).isEqualTo(vo.value());
        assertThat(entity.getStartDate()).isEqualTo(vo.startDate());
        assertThat(entity.getEndDate()).isEqualTo(vo.endDate().orElse(null));
        assertThat(entity.getAccountId()).isEqualTo(accountId);
    }

    @Test
    void toEntity_returnsNullForNullVO() {
        assertThat(BlacklistItemMapperJava.toEntity(null, 100L)).isNull();
    }
}
