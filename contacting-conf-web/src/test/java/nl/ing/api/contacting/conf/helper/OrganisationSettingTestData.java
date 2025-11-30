package nl.ing.api.contacting.conf.helper;


import com.ing.api.contacting.dto.java.resource.organisation.FlatOrganisationUnitDto;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.OrganisationSettingVO;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSettingDto;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSettingDtos;
import nl.ing.api.contacting.conf.domain.entity.OrganisationSettingsEntity;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;

import java.util.List;
import java.util.Optional;

public class OrganisationSettingTestData {

    private OrganisationSettingTestData() {
        // Prevent instantiation of utility class
    }

    public static final List<OrganisationSettingVO> ALL_ORG_SETTINGS = List.of(
            new OrganisationSettingVO(
                    Optional.of(1L), "key1", "value1", 1L, 1L, false, List.of()),
            new OrganisationSettingVO(
                    Optional.of(2L), "key1", "value2", 1L, 2L, true, List.of()),
            new OrganisationSettingVO(
                    Optional.of(3L), "key1", "value3", 1L, 3L, false, List.of()),
            new OrganisationSettingVO(
                    Optional.of(4L), "key2", "value4", 1L, 2L, true, List.of()),
            new OrganisationSettingVO(
                    Optional.of(5L), "key2", "value5", 1L, 3L, false, List.of()),
            new OrganisationSettingVO(
                    Optional.of(6L), "key3", "value6", 1L, 3L, true, List.of()),
            new OrganisationSettingVO(
                    Optional.of(7L), "key4", "value4", 1L, 3L, true, List.of()),
            new OrganisationSettingVO(
                    Optional.of(8L), "key1", "value7", 1L, 8L, true, List.of()),
            new OrganisationSettingVO(
                    Optional.of(9L), "key1", "value8", 1L, 9L, false, List.of()),
            new OrganisationSettingVO(
                    Optional.of(10L), "key2", "value9", 1L, 9L, true, List.of())
    );

    public static OrganisationSettingDto getOrganisationSettingDto() {
        return new OrganisationSettingDto(
                Optional.of(1L),
                "TEST_KEY",
                "value",
                1L,
                1L,
                true,
                Optional.of(List.of("CHAT", "VIDEO"))
        );
    }

    public static OrganisationSettingDto getOrganisationSettingDto1() {
        return new OrganisationSettingDto(
                Optional.of(1L),
                "key",
                "value",
                1L,
                1L,
                true,
                Optional.of(List.of("capability1", "capability2"))
        );
    }

    public static OrganisationSettingDtos getOrganisationSettingDtos() {
        return new OrganisationSettingDtos(
                List.of(getOrganisationSettingDto(), getOrganisationSettingDto1())
        );
    }

    public static OrganisationSettingsEntity getOrganisationSettingsEntity() {
        return new OrganisationSettingsEntity(1L,
                "TIMEZONE",
                "Europe/Amsterdam",
                1L,
                1L,
                true,
                "chat");
    }

    public static OrganisationSettingVO getOrganisationSettingVO() {
        return new OrganisationSettingVO(
                Optional.of(1L),
                "FOO123",
                "Asia/Delhi",
                1L,
                1L,
                false,
                List.of(SettingCapability.CHAT));
    }

    public static FlatOrganisationUnitDto getFlatOrganisationUnitDto() {
        return new FlatOrganisationUnitDto(8,
                "CLT5",
                3,
                "C2",
                1,
                "SC1");
    }

    public static OrganisationEntity getOrganisationEntity() {

        return OrganisationEntity.builder()
                .id(1L)
                .name("Child Org")
                .accountId(1L)
                .parentId(1L)
                .build();

    }
}
