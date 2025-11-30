package nl.ing.api.contacting.conf.domain.model.surveysetting;

import nl.ing.api.contacting.conf.domain.validation.SurverySettingValidation;

import java.util.Optional;

public record SurveyPhoneNumberFormatVO(
        Optional<Long> id,
        long surveyId,
        String format,
        String direction
) {
    public SurveyPhoneNumberFormatVO {
        SurverySettingValidation.validateId(id);
        SurverySettingValidation.validateSurveyId(surveyId);
        SurverySettingValidation.validateFormat(format);
        SurverySettingValidation.validateDirection(direction);
    }
}


