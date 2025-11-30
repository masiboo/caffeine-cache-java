package nl.ing.api.contacting.conf.mapper;

import io.micrometer.common.util.StringUtils;
import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.SurveyOrgMappingEntity;
import nl.ing.api.contacting.conf.domain.entity.SurveyPhoneNumberFormatEntity;
import nl.ing.api.contacting.conf.domain.entity.SurveySettingsEntity;
import nl.ing.api.contacting.conf.domain.entity.SurveyTaskQueueMappingEntity;
import nl.ing.api.contacting.conf.domain.entity.cassandra.SurveyCallRecordEntity;
import nl.ing.api.contacting.conf.domain.entity.cassandra.SurveyCallRecordKey;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyCallRecordVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyDetails;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyDetailsVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyOrgDetails;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyOrgMappingVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyPhoneNumberFormatVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveySettingVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyTaskQMappingVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyTaskQMappingWithName;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyUpdateVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyDetailsDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyOrgMappingDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyOrganisationDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyOverviewDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyPhoneNumberFormatDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyPhoneNumberFormatUpdateDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveySettingDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyTaskQMappingDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyUpdateDTO;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.java.domain.OrganisationVO;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Optional;

import static nl.ing.api.contacting.conf.util.ValidationUtils.validatedLong;
import static nl.ing.api.contacting.conf.util.ValidationUtils.validatedOptionalLong;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SurveyMapperJava {

    private static final String MSG_SURVEY_NAME_MISSING = "Survey name is missing";
    private static final String DIRECTION_ALLOWED = "allowed";
    private static final String DIRECTION_EXCLUDED  = "excluded";

    public static SurveySettingVO surveySettingEntityToVo(SurveySettingsEntity entity) {
        if (entity == null) throw Errors.valueMissing("SurveySettingsEntity is null");
        if (entity.getId() == null) throw Errors.valueMissing("Survey ID is missing");
        if (StringUtils.isBlank(entity.getName())) throw Errors.valueMissing(MSG_SURVEY_NAME_MISSING);

        return new SurveySettingVO(
                Optional.of(entity.getId()),
                entity.getAccountId(),
                entity.getName(),
                entity.getChannel(),
                entity.getChannelDirection(),
                entity.getVoiceSurveyId(),
                Optional.ofNullable(entity.getCallflowName()),
                Optional.ofNullable(entity.getMinFrequency()),
                Optional.ofNullable(entity.getDelay()),
                Optional.ofNullable(entity.getSurveyOfferRatio()),
                Optional.ofNullable(entity.getMinContactLength()),
                entity.getSurveyForTransfers()
        );
    }

    public static SurveySettingsEntity surveySettingVoToEntity(SurveySettingVO vo) {
        if (vo == null) throw Errors.valueMissing("SurveySettingVO is null");
        if (vo.name() == null || vo.name().isBlank()) throw Errors.valueMissing(MSG_SURVEY_NAME_MISSING);

        return SurveySettingsEntity.builder()
                .id(vo.id().orElse(null))
                .accountId(vo.accountId())
                .name(vo.name())
                .channel(vo.channel())
                .channelDirection(vo.channelDirection())
                .voiceSurveyId(vo.voiceSurveyId())
                .callflowName(vo.callflowName().orElse(null))
                .minFrequency(vo.minFrequency().orElse(null))
                .delay(vo.delay().orElse(null))
                .surveyOfferRatio(vo.surveyOfferRatio().orElse(null))
                .minContactLength(vo.minContactLength().orElse(null))
                .surveyForTransfers(vo.surveyForTransfers())
                .build();
    }

    public static SurveyOverviewDTO surveySettingVoToOverviewDTO(SurveySettingVO vo) {
        return new SurveyOverviewDTO(
                vo.id(),
                vo.name(),
                vo.channel(),
                vo.voiceSurveyId()
        );
    }

    public static List<SurveyOverviewDTO> surveySettingVoToOverviewDTO(List<SurveySettingVO> vos) {
        return vos.stream().map(SurveyMapperJava::surveySettingVoToOverviewDTO).toList();
    }

    public static SurveyPhoneNumberFormatVO surveyPhNumFormatEntityToVo(SurveyPhoneNumberFormatEntity entity) {
        if (entity == null) throw Errors.valueMissing("SurveyPhoneNumberFormatEntity is null");
        if (entity.getId() == null) throw Errors.valueMissing("PhoneNumberFormat ID is missing");

        String direction = !entity.isDirection() ? DIRECTION_ALLOWED : DIRECTION_EXCLUDED;
        return new SurveyPhoneNumberFormatVO(
                Optional.of(entity.getId()),
                entity.getSurveyId(),
                entity.getFormat(),
                direction
        );
    }

    public static SurveyPhoneNumberFormatEntity surveyPhNumFormatVoToEntity(SurveyPhoneNumberFormatVO vo) {
        if (vo == null) throw Errors.valueMissing("SurveyPhoneNumberFormatVO is null");
        if (vo.surveyId() <= 0) throw Errors.badRequest("Invalid survey ID for phone format");
        if (vo.direction() == null) throw Errors.badRequest("Direction is missing for phone format");

        return SurveyPhoneNumberFormatEntity.builder()
                .id(vo.id().orElse(null))
                .surveyId(vo.surveyId())
                .format(vo.format())
                .direction(!DIRECTION_ALLOWED.equals(vo.direction()))
                .build();
    }

    public static SurveyPhoneNumberFormatDTO surveyPhNumFormatVoToDto(SurveyPhoneNumberFormatVO vo) {
        if (vo == null) throw Errors.valueMissing("SurveyPhoneNumberFormatVO is null");
        return new SurveyPhoneNumberFormatDTO(vo.id(), vo.format());
    }

    public static SurveyTaskQMappingVO surveyTaskQModelToVo(SurveyTaskQMappingWithName model) {
        if (model == null || model.mapping() == null)
            throw Errors.valueMissing("SurveyTaskQMappingWithName or mapping is null");

        // Use the correct entity type
        SurveyTaskQueueMappingEntity mapping = model.mapping();
        String friendlyName = model.friendlyName();
        return new SurveyTaskQMappingVO(
                Optional.ofNullable(mapping.getId()),
                mapping.getSurveyId(),
                mapping.getTaskQueueId(),
                friendlyName
        );
    }

    public static SurveyTaskQMappingDTO surveyTaskQVOToDTO(SurveyTaskQMappingVO vo) {
        return new SurveyTaskQMappingDTO(vo.taskQId(), vo.tqName());
    }

    public static SurveyOrgMappingVO surveyOrgModelToVo(SurveyOrgDetails model) {
        if (model == null || model.mapping() == null) throw Errors.valueMissing("SurveyOrgDetails or mapping is null");

        SurveyOrgMappingEntity mapping = model.mapping(); // SurveyOrgMappingEntity
        return new SurveyOrgMappingVO(
                Optional.ofNullable(mapping.getId()),
                mapping.getSurveyId(),
                organisationMapper(model)
        );
    }

    private static OrganisationVO organisationMapper(SurveyOrgDetails model) {
        OrganisationEntity org = model.org();
        if (org == null) throw Errors.valueMissing("Organisation is null");

        OrganisationEntity parent = model.parent();
        OrganisationEntity grandParent = model.grandParent();

        // Only org (superCircle)
        if (parent == null && grandParent == null) {
            return new OrganisationVO(Optional.ofNullable(org.getId()), org.getName(), org.getOrgLevel(), Optional.empty(), null);
        }

        // Org with parent (circle)
        if (parent != null && grandParent == null) {
            OrganisationVO parentVO = new OrganisationVO(Optional.ofNullable(parent.getId()), parent.getName(), parent.getOrgLevel(), Optional.empty(), null);
            return new OrganisationVO(Optional.ofNullable(org.getId()), org.getName(), org.getOrgLevel(), Optional.of(parentVO), null);
        }

        // Org with parent and grandparent
        if (parent != null) {
            OrganisationVO grandParentVO = new OrganisationVO(Optional.ofNullable(grandParent.getId()), grandParent.getName(), grandParent.getOrgLevel(), Optional.empty(), null);
            OrganisationVO parentVO = new OrganisationVO(Optional.ofNullable(parent.getId()), parent.getName(), parent.getOrgLevel(), Optional.of(grandParentVO), null);
            return new OrganisationVO(Optional.ofNullable(org.getId()), org.getName(), org.getOrgLevel(), Optional.of(parentVO), null);
        }

        throw Errors.unexpected("Unexpected organisation mapping structure");
    }

    public static SurveyOrganisationDTO orgVoToDto(OrganisationVO vo) {
        return new SurveyOrganisationDTO(vo.name(), vo.organisationLevel().toString(), vo.parent().map(SurveyMapperJava::orgVoToDto).orElse(null));
    }

    public static SurveyOrgMappingDTO surveyOrgVOToDTO(SurveyOrgMappingVO vo) {
        return new SurveyOrgMappingDTO(vo.organisationVO().id().orElse(null), orgVoToDto(vo.organisationVO()));
    }

    public static SurveySettingDTO surveySettingVOToSettingDto(SurveySettingVO vo) {
        return new SurveySettingDTO(
                vo.id(),
                vo.name(),
                vo.channel(),
                vo.channelDirection(),
                vo.voiceSurveyId(),
                vo.callflowName(),
                vo.minFrequency(),
                vo.delay(),
                vo.surveyOfferRatio(),
                vo.minContactLength(),
                vo.surveyForTransfers()
        );
    }

    public static SurveySettingVO surveySettingDTOToVo(SurveySettingDTO dto, long accountId) {
        if (dto == null) throw Errors.valueMissing("SurveySettingDTO is null");
        if (dto.name() == null || dto.name().isBlank()) throw Errors.valueMissing(MSG_SURVEY_NAME_MISSING);

        return new SurveySettingVO(
                validatedOptionalLong(dto.id()),
                validatedLong(accountId),
                dto.name(),
                HtmlUtils.htmlEscape(dto.channel()),
                HtmlUtils.htmlEscape(dto.channelDirection()),
                HtmlUtils.htmlEscape(dto.voiceSurveyId()),
                dto.callflowName().map(HtmlUtils::htmlEscape),
                dto.minFrequency(),
                dto.delay(),
                dto.surveyOfferRatio(),
                dto.minContactLength(),
                dto.surveyForTransfers()
        );
    }

    public static SurveyUpdateVO surveyUpdateDTOToVo(SurveyUpdateDTO dto, long accountId, long surveyId) {
        if (dto == null) throw Errors.valueMissing("SurveyUpdateDTO is null");

        SurveySettingVO settingVo = surveySettingDTOToVo(dto.settings(), validatedLong(accountId));
        List<SurveyPhoneNumberFormatVO> formatsAdded = dto.formatsAdded().stream()
                .map(f -> surveyPhoneFormatUpdateDTOToVO(f, surveyId))
                .toList();
        List<SurveyPhoneNumberFormatVO> formatsRemoved = dto.formatsRemoved().stream()
                .map(f -> surveyPhoneFormatUpdateDTOToVO(f, surveyId))
                .toList();

        return new SurveyUpdateVO(settingVo, formatsAdded, formatsRemoved);
    }

    public static SurveyPhoneNumberFormatVO surveyPhoneFormatUpdateDTOToVO(SurveyPhoneNumberFormatUpdateDTO dto, long surveyId) {
        if (dto == null) throw Errors.valueMissing("SurveyPhoneNumberFormatUpdateDTO is null");

        String direction = dto.direction() == 0 ? DIRECTION_ALLOWED : DIRECTION_EXCLUDED;
        return new SurveyPhoneNumberFormatVO(validatedOptionalLong(dto.id()), surveyId, dto.format(), direction);
    }

    public static SurveyDetailsVO surveyDetailsToVo(SurveyDetails surveyDetails) {
        if (surveyDetails == null) throw Errors.valueMissing("SurveyDetails is null");

        return new SurveyDetailsVO(
                surveySettingEntityToVo(surveyDetails.setting()),
                surveyDetails.phNumFormat().stream().map(SurveyMapperJava::surveyPhNumFormatEntityToVo).toList(),
                surveyDetails.taskQMapping().stream().map(SurveyMapperJava::surveyTaskQModelToVo).toList(),
                surveyDetails.orgMapping().stream().map(SurveyMapperJava::surveyOrgModelToVo).toList()
        );
    }

    public static SurveyDetailsDTO surveyDetailsVOToOverviewDto(SurveyDetailsVO surveyDetailsVO) {
        SurveySettingDTO settingDTO = surveySettingVOToSettingDto(surveyDetailsVO.surveySettingVO());

        List<SurveyPhoneNumberFormatVO> allowed = surveyDetailsVO.phNumFormats().stream().filter(f -> DIRECTION_ALLOWED.equals(f.direction())).toList();
        List<SurveyPhoneNumberFormatVO> excluded = surveyDetailsVO.phNumFormats().stream().filter(f -> !DIRECTION_ALLOWED.equals(f.direction())).toList();
        List<SurveyPhoneNumberFormatDTO> allowedPhNumDto = allowed.stream().map(SurveyMapperJava::surveyPhNumFormatVoToDto).toList();
        List<SurveyPhoneNumberFormatDTO> excludedPhNumDto = excluded.stream().map(SurveyMapperJava::surveyPhNumFormatVoToDto).toList();
        List<SurveyTaskQMappingDTO> taskQMappingDto = surveyDetailsVO.taskQMappingVO().stream().map(SurveyMapperJava::surveyTaskQVOToDTO).toList();
        List<SurveyOrgMappingDTO> orgMappingDto = surveyDetailsVO.orgMappingVO().stream().map(SurveyMapperJava::surveyOrgVOToDTO).toList();

        return new SurveyDetailsDTO(settingDTO, allowedPhNumDto, excludedPhNumDto, taskQMappingDto, orgMappingDto);
    }

    public static SurveyCallRecordVO surveyCallRecordEntityToVO(SurveyCallRecordEntity entity) {
        SurveyCallRecordKey key = entity.getKey();
        return new SurveyCallRecordVO(
                key.getAccountFriendlyName(),
                key.getPhoneNum(),
                entity.getSurveyName(),
                key.getOfferedDatetime()
        );
    }
}
