package nl.ing.api.contacting.conf.domain.model.surveysetting.dto;

import java.util.List;

public record SurveyDetailsDTO(
    SurveySettingDTO settings,
    List<SurveyPhoneNumberFormatDTO> allowedPhNumFormats,
    List<SurveyPhoneNumberFormatDTO> excludedPhNumFormats,
    List<SurveyTaskQMappingDTO> taskQMapping,
    List<SurveyOrgMappingDTO> orgMapping
) {}

