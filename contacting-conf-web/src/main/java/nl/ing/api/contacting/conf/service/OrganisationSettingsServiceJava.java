package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.audit.AuditedEntity;
import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.organisation.FlatOrganisationUnitDto;
import com.ing.api.contacting.dto.java.resource.organisation.settings.EmployeeOrganisationSettings;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSetting;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSettingDto;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.OrganisationSettingsEntity;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.OrganisationSettingVO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataVO;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.OrganisationSettingMapperJava;
import nl.ing.api.contacting.conf.repository.OrganisationSettingsAuditRepository;
import nl.ing.api.contacting.conf.repository.OrganisationSettingsJpaRepository;
import nl.ing.api.contacting.trust.rest.feature.permissions.RestrictionOrganisationalUnitDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganisationSettingsServiceJava {

    private final OrganisationSettingsAuditRepository organisationSettingsRepositoryWrapper;

    private final SettingsMetadataServiceJava settingsMetadataService;

    private final OrganisationSettingsJpaRepository organisationSettingsRepository;

    /**
     * Fetches organisation settings by orgIds and accountId, returns mapped VO list.
     * Returns empty list if repository returns null or empty.
     */
    public List<OrganisationSettingVO> getOrganisationSettingsByOrgIds(Set<Long> orgIds, Long accountId) {
        List<OrganisationSettingsEntity> organisationSettingsEntities = organisationSettingsRepository.findByOrgIdInAndAccountId(orgIds, accountId);
        return organisationSettingsEntities.stream()
                .map(OrganisationSettingMapperJava::toVO)
                .toList();
    }

    /**
     * Get all organisation settings filtered by capability.
     */
    public List<OrganisationSettingVO> getOrganisationSettings(Set<SettingCapability> capabilities, ContactingContext context) {
        return organisationSettingsRepositoryWrapper.findByCapabilities(capabilities, context.accountId()).stream()
                .map(OrganisationSettingMapperJava::toVO)
                .toList();

    }

    @Transactional(readOnly = true)
    public OrganisationSettingVO createOrganisationSetting(OrganisationSettingDto orgSetting, ContactingContext context) {
        return saveOrganisationSetting(OrganisationSettingMapperJava.fromDto(orgSetting), context);
    }

    @Transactional(readOnly = true)
    public OrganisationSettingVO updateOrganisationSetting(OrganisationSettingDto orgSetting, Long id, ContactingContext context) {

        OrganisationSettingDto updatedDto = orgSetting.withId(id);
        return saveOrganisationSetting(OrganisationSettingMapperJava.fromDto(updatedDto), context);
    }
    /*
     * Save or update an organisation setting.
     */

    @Transactional(readOnly = true)
    public OrganisationSettingVO saveOrganisationSetting(OrganisationSettingVO orgSetting, ContactingContext context) {
        Optional<SettingsMetadataVO> metaOpt = settingsMetadataService.findByName(orgSetting.key());
        if (metaOpt.isEmpty()) {
            throw Errors.notFound("Requested key is not present in settings metadata");
        }
        // Create new record with the capability from metadata
        OrganisationSettingVO settingWithCapability = orgSetting.withCapability(metaOpt.get().capability());
        OrganisationSettingVO validOrgSetting = validateDataWithMetaData(settingWithCapability, metaOpt.get());
        OrganisationSettingsEntity orgSettingsEntity = OrganisationSettingMapperJava.toEntity(validOrgSetting);

        organisationSettingsRepositoryWrapper.findByOrganisationId(orgSetting.orgId())
                .orElseThrow(() -> Errors.notFound("Organisation Entity not found"));

        if (validOrgSetting.id().isPresent()) {
            Long id = validOrgSetting.id().get();
            return updateOrganisationSetting(id, orgSettingsEntity, context);
        } else {
            OrganisationSettingsEntity organisationSettingsEntity = organisationSettingsRepositoryWrapper.saveAndAudit(
                    orgSettingsEntity, context);
            return OrganisationSettingMapperJava.toVO(organisationSettingsEntity);
        }
    }

    public EmployeeOrganisationSettings getOrganisationSettingsForEmployee(
            ContactingContext context,
            scala.collection.immutable.Set<RestrictionOrganisationalUnitDto> restrictionPerOrganisations,
            scala.Option<com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto> flatOrganisationUnitDtoSet
    ) {
        Set<com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto> orgIdsScala =
                scala.jdk.javaapi.CollectionConverters.asJava(restrictionPerOrganisations)
                        .stream()
                        .map(RestrictionOrganisationalUnitDto::org)
                        .collect(Collectors.toSet());

        Set<com.ing.api.contacting.dto.java.resource.organisation.FlatOrganisationUnitDto>
                flatOrganisationDtoJava = OrganisationSettingMapperJava.scalaDtoToJavaDto(orgIdsScala);

        Optional<com.ing.api.contacting.dto.java.resource.organisation.FlatOrganisationUnitDto>
                flatOrganisationDtoJavaOptional = OrganisationSettingMapperJava.scalaDtoToJavaDtoOpt(flatOrganisationUnitDtoSet);


        List<OrganisationSettingVO> allOrgSettings = getOrganisationSettings(context);
        List<OrganisationSettings> settings = getSettingsForEmployee(context, allOrgSettings, flatOrganisationDtoJava);

        List<OrganisationSettings> primarySettings = flatOrganisationDtoJavaOptional
                .map(org -> settings.stream()
                        .filter(s -> s.orgId() == org.cltId())
                        .toList())
                .orElseGet(() -> settings.stream()
                        .filter(s -> s.orgId() == 1)
                        .toList());

        return new EmployeeOrganisationSettings(settings, primarySettings);
    }

    public List<OrganisationSettingVO> getOrganisationSettingsWithCapabilities(String capabilities, ContactingContext context) {
        Set<SettingCapability> enumCapabilities = Arrays.stream(capabilities.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .map(SettingCapability::fromValue)
                .collect(Collectors.toSet());

        return getOrganisationSettings(enumCapabilities, context);
    }
    public List<OrganisationSettingVO> getOrganisationSettingsWithOutCapabilities(ContactingContext context) {
        return getOrganisationSettings(context);
    }


    public List<OrganisationSettingVO> getOrganisationSettings(ContactingContext context) {
        return organisationSettingsRepositoryWrapper.findAll(context).stream()
                .map(OrganisationSettingMapperJava::toVO)
                .toList();
    }

    private List<OrganisationSettings> getSettingsForEmployee(
            ContactingContext context,
            List<OrganisationSettingVO> allOrgSettings,
            Set<FlatOrganisationUnitDto> organisations
    ) {
        return organisations.stream().map(organisation -> {
            List<OrganisationSetting> accountSettings = organisationSettingsRepositoryWrapper.findAll(context)
                    .stream()
                    .map(setting -> new OrganisationSetting(setting.getKey(), true, setting.getValue()))
                    .toList();

            List<OrganisationSetting> orgSettings = getSettingsForOrganisation(allOrgSettings, organisation)
                    .stream()
                    .map(s -> new OrganisationSetting(s.key(), true, s.value()))
                    .toList();

            List<OrganisationSetting> allSettings = Stream.concat(orgSettings.stream(), accountSettings.stream())
                    .collect(Collectors.groupingBy(
                            OrganisationSetting::name,
                            Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0))))
                    .values()
                    .stream()
                    .toList();

            return new OrganisationSettings(organisation.cltId(), allSettings);
        }).toList();
    }

    private OrganisationSettingVO updateOrganisationSetting(Long id, OrganisationSettingsEntity orgSettingsEntity, ContactingContext context) {

        organisationSettingsRepositoryWrapper.findById(id, context).orElseThrow(() ->
                Errors.notFound("Organisation setting with id not found: %d".formatted(id)));

        OrganisationSettingsEntity organisationSettingsEntity = organisationSettingsRepositoryWrapper.updateAndAudit(
                orgSettingsEntity, context);

        return OrganisationSettingMapperJava.toVO(organisationSettingsEntity);
    }

    private OrganisationSettingVO validateDataWithMetaData(OrganisationSettingVO orgSetting, SettingsMetadataVO meta) {
        if (SettingsMetadataServiceJava.regexDontMatch(meta, orgSetting.value())) {

            throw Errors.badRequest("Requested value does not match regex %s in settings metadata".formatted(meta.regex()));

        } else if (SettingsMetadataServiceJava.optionsDontMatch(meta, orgSetting.value())) {

            throw Errors.badRequest("Requested value is not present in settings metadata");

        } else {
            // Create new record with the capability from metadata
            return orgSetting.withCapability(meta.capability());
        }
    }

    /*
     * Delete an organisation setting by its id.
     */

    public void deleteOrganisationSetting(long id, ContactingContext context) {
        findById(id, context);
        organisationSettingsRepositoryWrapper.deleteById(id);
    }

    @Transactional(readOnly = true)
    public OrganisationSettingsEntity findById(Long id, ContactingContext context) {
        Optional<OrganisationSettingsEntity> organisationSettingsEntity = organisationSettingsRepositoryWrapper.findById(id, context);

        if (organisationSettingsEntity.isEmpty()) {
            throw Errors.notFound("organisation setting with id " + id + " not found");
        }
        return organisationSettingsEntity.get();

    }

    /**
     * Get audited versions for organisation settings.
     */

    public List<AuditedEntity<OrganisationSettingsEntity, Long>> getAuditedVersions(long entityId, int numRows, ContactingContext context) {
        List<AuditedEntity<OrganisationSettingsEntity, Long>> auditHistory = organisationSettingsRepositoryWrapper.getAuditHistory(entityId, numRows, context);

        return OrganisationSettingMapperJava.auditedEntityToAuditedEntity(auditHistory);
    }

    /**
     * Get settings for an organisation by filtering from all settings
     *
     * @param allOrgSettings        All organization settings
     * @param organisationalUnitDto Organization unit data
     * @return List of organization settings for the organization
     */

    public static List<OrganisationSettingVO> getSettingsForOrganisation(List<OrganisationSettingVO> allOrgSettings,
                                                                         FlatOrganisationUnitDto organisationalUnitDto) {
        List<OrganisationSettingVO> teamLevelSettings =
                getOrganisationSettingsForOrg(allOrgSettings, organisationalUnitDto.cltId());
        List<String> teamSettingKeys = teamLevelSettings.stream().map(OrganisationSettingVO::key).toList();
        List<OrganisationSettingVO> circleLevelSettings = getOrganisationSettingsForOrg(
                allOrgSettings.stream()
                        .filter(x -> !teamSettingKeys.contains(x.key()))
                        .toList(),
                organisationalUnitDto.circleId());
        List<String> circleSettingKeys = circleLevelSettings.stream().map(OrganisationSettingVO::key).toList();
        List<OrganisationSettingVO> superCircleLevelSettings = getOrganisationSettingsForOrg(
                allOrgSettings.stream()
                        .filter(x -> !(teamSettingKeys.contains(x.key()) && circleSettingKeys.contains(x.key())))
                        .toList(),
                organisationalUnitDto.superCircleId());

        List<OrganisationSettingVO> result = new ArrayList<>();
        result.addAll(teamLevelSettings);
        result.addAll(circleLevelSettings);
        result.addAll(superCircleLevelSettings);
        return result;
    }


    public static List<OrganisationSettingVO> getOrganisationSettingsForOrg(
            List<OrganisationSettingVO> allOrgSettings,
            long orgId
    ) {
        return allOrgSettings.stream()
                .filter(setting -> setting.orgId() == orgId)
                .toList();
    }

}