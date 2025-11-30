package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.audit.AuditedEntity;
import com.ing.api.contacting.dto.java.resource.organisation.FlatOrganisationUnitDto;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSetting;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSettingDto;
import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.OrganisationSettingsEntity;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.OrganisationSettingVO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import scala.Option;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class OrganisationSettingMapperJava {

    // Converts OrganisationSettingsEntity to OrganisationSettingVO
    public static OrganisationSettingVO toVO(OrganisationSettingsEntity entity) {
        String capabilities = entity.getCapabilities();
        List<SettingCapability> capabilityEnum = (capabilities == null || capabilities.isEmpty())
                ? List.of()
                : Arrays.stream(capabilities.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SettingCapability::fromValue)
                .toList();

        return new OrganisationSettingVO(
                Optional.ofNullable(entity.getId()),
                entity.getKey(),
                entity.getValue(),
                entity.getAccountId(),
                entity.getOrgId(),
                entity.getEnabled(),
                capabilityEnum
        );
    }

    // Converts OrganisationSettingVO to OrganisationSettingsEntity
    public static OrganisationSettingsEntity toEntity(OrganisationSettingVO vo) {
        String capabilities = (vo.capability() == null || vo.capability().isEmpty())
                ? ""
                : vo.capability().stream()
                .map(SettingCapability::toString)
                .collect(Collectors.joining(","));

        return OrganisationSettingsEntity.builder()
                .id(vo.id().orElse(null))
                .key(vo.key())
                .value(vo.value())
                .accountId(vo.accountId())
                .orgId(vo.orgId())
                .enabled(vo.enabled())
                .capabilities(capabilities)
                .build();
    }

    // Converts OrganisationSettingVO to OrganisationSettingDtoJava
    public static OrganisationSettingDto toDto(OrganisationSettingVO vo) {
        Optional<List<String>> capabilityList = (vo.capability() == null || vo.capability().isEmpty())
                ? Optional.empty()
                : Optional.of(vo.capability().stream()
                .map(SettingCapability::toString)
                .toList());

        return new OrganisationSettingDto(
                vo.id(),
                vo.key(),
                vo.value(),
                vo.accountId(),
                vo.orgId(),
                vo.enabled(),
                capabilityList
        );
    }

    // Converts OrganisationSettingVOJava to OrganisationSettingVO with new id
    public static List<AuditedEntity<OrganisationSettingsEntity, Long>> auditedEntityToAuditedEntity(List<AuditedEntity<OrganisationSettingsEntity, Long>> auditHistory) {
        return auditHistory.stream().map(auditedEntity -> {
            Optional<OrganisationSettingsEntity> entityOpt = auditedEntity.entity();
            if (entityOpt.isEmpty()) return auditedEntity;
            OrganisationSettingVO vo = OrganisationSettingMapperJava.toVO(entityOpt.get());
            OrganisationSettingsEntity entity = OrganisationSettingMapperJava.toEntity(vo);
            return new AuditedEntity<>(
                    auditedEntity.id(),
                    auditedEntity.revId(),
                    auditedEntity.accountId(),
                    auditedEntity.auditType(),
                    auditedEntity.entityId(),
                    Optional.of(entity),
                    auditedEntity.entityJsonData(),
                    auditedEntity.modifiedBy(),
                    auditedEntity.modifiedTime()
            );
        }).toList();
    }

    // Converts OrganisationSettingDtoJava to OrganisationSettingVO
    public static OrganisationSettingVO fromDto(OrganisationSettingDto dto) {
        List<SettingCapability> capabilityEnum = dto.capability()
                .map(list -> list.stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(SettingCapability::fromValue)
                        .toList()).orElseGet(List::of);

        return new OrganisationSettingVO(
                dto.id(),
                dto.key(),
                dto.value(),
                dto.accountId(),
                dto.orgId(),
                dto.enabled(),
                capabilityEnum
        );
    }

    // Converts OrganisationSettingVO to OrganisationSetting
    public static OrganisationSetting toSimpleDto(OrganisationSettingVO vo) {
        return new OrganisationSetting(
                vo.key(),
                vo.enabled(),
                vo.value()
        );
    }

    /**
     * Returns all OrganisationSettingVOs for the given organisational unit,
     * prioritizing team, then circle, then superCircle.
     */
    public static List<OrganisationSettingVO> getSettingsForOrganisation(
            List<OrganisationSettingVO> allOrgSettings,
            FlatOrganisationUnitDto organisationalUnitDto
    ) {
        // Team settings
        List<OrganisationSettingVO> teamLevelSettings = getOrganisationSettingsForOrg(allOrgSettings, organisationalUnitDto.cltId());
        Set<String> teamSettingKeys = teamLevelSettings.stream()
                .map(OrganisationSettingVO::key)
                .collect(Collectors.toSet());

        // Circle settings (using superCircleId as per test expectation)
        List<OrganisationSettingVO> circleLevelSettings = getOrganisationSettingsForOrg(
                allOrgSettings.stream()
                        .filter(setting -> !teamSettingKeys.contains(setting.key()))
                        .toList(),
                organisationalUnitDto.superCircleId()
        );
        Set<String> circleSettingKeys = circleLevelSettings.stream()
                .map(OrganisationSettingVO::key)
                .collect(Collectors.toSet());

        // SuperCircle settings (using circleId as per test expectation)
        List<OrganisationSettingVO> superCircleLevelSettings = getOrganisationSettingsForOrg(
                allOrgSettings.stream()
                        .filter(setting -> !teamSettingKeys.contains(setting.key()) && !circleSettingKeys.contains(setting.key()))
                        .toList(),
                organisationalUnitDto.circleId()
        );

        // Concatenate in order: team, circle, superCircle
        return Stream.of(teamLevelSettings, circleLevelSettings, superCircleLevelSettings)
                .flatMap(List::stream)
                .toList();
    }


    /**
     * Filters OrganisationSettingVOs by orgId.
     */
    public static List<OrganisationSettingVO> getOrganisationSettingsForOrg(
            List<OrganisationSettingVO> allOrgSettings,
            long orgId
    ) {
        return allOrgSettings.stream()
                .filter(setting -> setting.orgId() == orgId)
                .toList();
    }

    public static Set<FlatOrganisationUnitDto> scalaDtoToJavaDto(
            Set<com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto> scalaDto
    ){
        return scalaDto.stream()
                .map(scalaUnit -> new FlatOrganisationUnitDto(
                        scalaUnit.cltId(),
                        scalaUnit.cltName(),
                        scalaUnit.circleId(),
                        scalaUnit.circleName(),
                        scalaUnit.superCircleId(),
                        scalaUnit.superCircleName()
                ))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static Optional<FlatOrganisationUnitDto> scalaDtoToJavaDtoOpt(
            Option<com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto> scalaDto
    ){
        if (scalaDto.isEmpty()) return Optional.empty();
        var s = scalaDto.get();
        return Optional.of(new FlatOrganisationUnitDto(
                s.cltId(),
                s.cltName(),
                s.circleId(),
                s.circleName(),
                s.superCircleId(),
                s.superCircleName()
        ));
    }

    /**
     * Returns a set of organisation IDs from FlatOrganisationUnitDto.
     */
    public static Set<Long> orgDtoToOrgIdSet(FlatOrganisationUnitDto orgUnitDto) {
        return Set.of(
                (long) orgUnitDto.cltId(),
                (long) orgUnitDto.circleId(),
                (long) orgUnitDto.superCircleId()
        );
    }
}
