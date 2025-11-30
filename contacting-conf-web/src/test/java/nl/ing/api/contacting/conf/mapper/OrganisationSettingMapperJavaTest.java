package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.organisation.FlatOrganisationUnitDto;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSetting;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSettingDto;
import nl.ing.api.contacting.conf.domain.entity.OrganisationSettingsEntity;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.OrganisationSettingVO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrganisationSettingMapperJavaTest {

    @Test
    @DisplayName("toVO should map OrganisationSettingsEntity to OrganisationSettingVO")
    void testToVO() {
        OrganisationSettingsEntity entity = OrganisationSettingsEntity.builder()
                .id(1L)
                .key("settingKey")
                .value("settingValue")
                .accountId(100L)
                .orgId(200L)
                .enabled(true)
                .capabilities("chat,video")
                .build();

        OrganisationSettingVO vo = OrganisationSettingMapperJava.toVO(entity);

        assertEquals(Optional.of(1L), vo.id());
        assertEquals("settingKey", vo.key());
        assertEquals("settingValue", vo.value());
        assertEquals(100L, vo.accountId());
        assertEquals(200L, vo.orgId());
        assertTrue(vo.enabled());
        assertEquals(List.of(SettingCapability.CHAT, SettingCapability.VIDEO), vo.capability());
    }

    @Test
    @DisplayName("toEntity should map OrganisationSettingVO to OrganisationSettingsEntity")
    void testToEntity() {
        OrganisationSettingVO vo = new OrganisationSettingVO(
                Optional.of(2L),
                "key2",
                "value2",
                101L,
                201L,
                false,
                List.of(SettingCapability.RECORDING)
        );

        OrganisationSettingsEntity entity = OrganisationSettingMapperJava.toEntity(vo);

        assertEquals(2L, entity.getId());
        assertEquals("key2", entity.getKey());
        assertEquals("value2", entity.getValue());
        assertEquals(101L, entity.getAccountId());
        assertEquals(201L, entity.getOrgId());
        assertFalse(entity.getEnabled());
        assertEquals("RECORDING", entity.getCapabilities());
    }

    @Test
    @DisplayName("toDto should map OrganisationSettingVO to OrganisationSettingDtoJava")
    void testToDto() {
        OrganisationSettingVO vo = new OrganisationSettingVO(
                Optional.of(3L),
                "key3",
                "value3",
                102L,
                202L,
                true,
                List.of(SettingCapability.CHAT)
        );

        OrganisationSettingDto dto = OrganisationSettingMapperJava.toDto(vo);

        assertEquals(Optional.of(3L), dto.id());
        assertEquals("key3", dto.key());
        assertEquals("value3", dto.value());
        assertEquals(102L, dto.accountId());
        assertEquals(202L, dto.orgId());
        assertTrue(dto.enabled());
        assertEquals(Optional.of(List.of("CHAT")), dto.capability());
    }

    @Test
    @DisplayName("fromDto should map OrganisationSettingDtoJava to OrganisationSettingVO")
    void testFromDto() {
        OrganisationSettingDto dto = new OrganisationSettingDto(
                Optional.of(4L),
                "key4",
                "value4",
                103L,
                203L,
                true,
                Optional.of(List.of("DIALER", "GENERIC"))
        );

        OrganisationSettingVO vo = OrganisationSettingMapperJava.fromDto(dto);

        assertEquals(Optional.of(4L), vo.id());
        assertEquals("key4", vo.key());
        assertEquals("value4", vo.value());
        assertEquals(103L, vo.accountId());
        assertEquals(203L, vo.orgId());
        assertTrue(vo.enabled()); // always true in mapper, see implementation
        assertEquals(List.of(SettingCapability.DIALER, SettingCapability.GENERIC), vo.capability());
    }

    @Test
    @DisplayName("toSimpleDto should map OrganisationSettingVO to OrganisationSetting")
    void testToSimpleDto() {
        OrganisationSettingVO vo = new OrganisationSettingVO(
                Optional.empty(),
                "simpleKey",
                "simpleValue",
                104L,
                204L,
                true,
                List.of()
        );

        OrganisationSetting simpleDto = OrganisationSettingMapperJava.toSimpleDto(vo);

        assertEquals("simpleKey", simpleDto.name());
        assertTrue(simpleDto.enabled());
        assertEquals("simpleValue", simpleDto.value());
    }

    @Test
    @DisplayName("getSettingsForOrganisation should prioritize team, circle, superCircle")
    void testGetSettingsForOrganisation() {
        OrganisationSettingVO teamSetting = new OrganisationSettingVO(
                Optional.of(1L), "teamKey", "teamValue", 1L, 10L, true, List.of());
        OrganisationSettingVO circleSetting = new OrganisationSettingVO(
                Optional.of(2L), "circleKey", "circleValue", 1L, 20L, true, List.of());
        OrganisationSettingVO superCircleSetting = new OrganisationSettingVO(
                Optional.of(3L), "superCircleKey", "superCircleValue", 1L, 30L, true, List.of());

        List<OrganisationSettingVO> allSettings = List.of(teamSetting, circleSetting, superCircleSetting);

        FlatOrganisationUnitDto orgUnit = new FlatOrganisationUnitDto(10, "cltName", 30, "circleName", 20, "superCircleName");

        List<OrganisationSettingVO> result = OrganisationSettingMapperJava.getSettingsForOrganisation(allSettings, orgUnit);

        assertEquals(3, result.size());
        assertTrue(result.contains(teamSetting));
        assertTrue(result.contains(circleSetting));
        assertTrue(result.contains(superCircleSetting));
        assertEquals("teamKey", result.get(0).key());
        assertEquals("circleKey", result.get(1).key());
        assertEquals("superCircleKey", result.get(2).key());
    }

    @Test
    @DisplayName("getOrganisationSettingsForOrg should filter by orgId")
    void testGetOrganisationSettingsForOrg() {
        OrganisationSettingVO s1 = new OrganisationSettingVO(Optional.of(1L), "k1", "v1", 1L, 100L, true, List.of());
        OrganisationSettingVO s2 = new OrganisationSettingVO(Optional.of(2L), "k2", "v2", 1L, 200L, true, List.of());
        OrganisationSettingVO s3 = new OrganisationSettingVO(Optional.of(3L), "k3", "v3", 1L, 100L, true, List.of());

        List<OrganisationSettingVO> allSettings = List.of(s1, s2, s3);

        List<OrganisationSettingVO> filtered = OrganisationSettingMapperJava.getOrganisationSettingsForOrg(allSettings, 100L);

        assertEquals(2, filtered.size());
        assertTrue(filtered.contains(s1));
        assertTrue(filtered.contains(s3));
    }

    @Test
    @DisplayName("orgDtoToOrgIdSet should return all org IDs from FlatOrganisationUnitDto")
    void testOrgDtoToOrgIdSet() {
        FlatOrganisationUnitDto orgUnit = new FlatOrganisationUnitDto(111, "cltName", 333, "circleName", 222, "superCircleName");

        Set<Long> ids = OrganisationSettingMapperJava.orgDtoToOrgIdSet(orgUnit);

        assertEquals(Set.of(111L, 222L, 333L), ids);
    }
}

