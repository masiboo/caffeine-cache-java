package nl.ing.api.contacting.conf.domain.model.surveysetting;

import nl.ing.api.contacting.conf.domain.validation.SurverySettingValidation;

import java.util.Optional;

public record SurveyTaskQMappingVO(
        Optional<Long> id,
        long surveyId,
        long taskQId,
        String tqName
) {
    public SurveyTaskQMappingVO {
        SurverySettingValidation.validateId(id);
        SurverySettingValidation.validateSurveyId(surveyId);
        SurverySettingValidation.validateTaskQId(taskQId);
        SurverySettingValidation.validateTqName(tqName);
    }
}

