package nl.ing.api.contacting.conf.domain.model.surveysetting;

import nl.ing.api.contacting.conf.domain.entity.SurveyPhoneNumberFormatEntity;
import nl.ing.api.contacting.conf.domain.entity.SurveySettingsEntity;

import java.util.List;

public record SurveyDetails(
        SurveySettingsEntity setting,
        List<SurveyPhoneNumberFormatEntity> phNumFormat,
        List<SurveyTaskQMappingWithName> taskQMapping,
        List<SurveyOrgDetails> orgMapping
) {}
